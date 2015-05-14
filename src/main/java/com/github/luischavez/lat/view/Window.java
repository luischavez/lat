/*
 * Copyright (C) 2015 Luis Chávez
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.luischavez.lat.view;

import com.github.luischavez.database.link.Row;
import com.github.luischavez.database.link.RowList;
import com.github.luischavez.lat.Context;
import com.github.luischavez.lat.controller.GroupController;
import com.github.luischavez.lat.controller.QualificationController;
import com.github.luischavez.lat.controller.StudentController;
import com.github.luischavez.lat.excel.Excel;
import com.github.luischavez.lat.util.Validators;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.LocalDateTime;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class Window extends JFrame {

    private final GroupController groupController = Context.controller(GroupController.class);
    private final StudentController studentController = Context.controller(StudentController.class);
    private final QualificationController qualificationController = Context.controller(QualificationController.class);

    public Window() {
        super("Laboratory Admin Tool");
        this.initComponents();
        this.configure();
        this.loadGroups();
        this.addListeners();
    }

    private void configure() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.pack();
        this.setResizable(false);
        this.setVisible(true);
        this.toggle(false);
    }

    private void loadGroups() {
        RowList groups = this.groupController.getAll();
        this.groupComboBox.removeAllItems();
        this.groupComboBox.addItem("-------");
        for (Row group : groups) {
            this.groupComboBox.addItem(group.value("name"));
        }
    }

    private long getGroupId() {
        String groupName = this.groupComboBox.getSelectedItem().toString();
        return this.groupController.getGroupId(groupName);
    }

    private void loadQualifications(long studentId) {
        DefaultTableModel model = (DefaultTableModel) this.qualificationsTable.getModel();
        model.setRowCount(0);
        Double[] qualifications = new Double[6];
        for (int i = 0; i < 6; i++) {
            double qualification = this.qualificationController.get(this.getGroupId(), studentId, i + 1);
            qualifications[i] = -1 == qualification ? 0 : qualification;
        }
        model.addRow(qualifications);
        double average = 0;
        for (Double qualification : qualifications) {
            average += qualification;
        }
        average /= qualifications.length;
        this.averageLabel.setText(String.format("Promedio: %.2f", average));
    }

    private void error(String title, Object message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void message(String title, Object message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean confirm(String title, Object message) {
        int confirm = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION);
        return JOptionPane.YES_OPTION == confirm;
    }

    private void toggle(boolean visible) {
        this.credentialLabel.setVisible(visible);
        this.studentNameLabel.setVisible(visible);
        this.qualificationButton.setVisible(visible);
        this.qualificationsPanel.setVisible(visible);
        this.averageLabel.setVisible(visible);

        this.pack();
    }

    private void addListeners() {
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent event) {
                Context.database().close();
            }
        });
        this.addStudentButton.addActionListener(this::onStudentSubmit);
        this.deleteStudentButton.addActionListener(this::onStudentDelete);
        this.addGroupButton.addActionListener(this::onGroupSubmit);
        this.deleteGroupButton.addActionListener(this::onGroupDelete);
        this.qualificationButton.addActionListener(this::onQualification);
        this.allExcelButton.addActionListener(this::onAllExcel);
        this.excelButton.addActionListener(this::onExcel);
        this.searchButton.addActionListener(this::onSearch);
        this.groupComboBox.addActionListener(this::onGroupSelected);
        this.studentsTable.getSelectionModel().addListSelectionListener(this::onStudentSelected);
        this.removeStudentMenuItem.addActionListener(this::onStudentRemove);
        this.credentialField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent event) {
                String credential = credentialField.getText();
                Row student = studentController.getByCredential(credential);
                String firstName = "";
                String lastName = "";
                if (null != student) {
                    firstName = student.value("first_name", String.class);
                    lastName = student.value("last_name", String.class);
                }
                firstNameField.setText(firstName);
                lastNameField.setText(lastName);
            }
        });
        this.studentsTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    JTable source = (JTable) event.getSource();
                    int row = source.rowAtPoint(event.getPoint());
                    int column = source.columnAtPoint(event.getPoint());

                    if (!source.isRowSelected(row)) {
                        source.changeSelection(row, column, false, false);
                    }

                    studentTablePopMenu.show(event.getComponent(), event.getX(), event.getY());
                }
            }
        });
    }

    private void onSearch(ActionEvent event) {
        String credential = this.credentialField.getText();
        String firstName = this.firstNameField.getText();
        String lastName = this.lastNameField.getText();

        long[] ids = this.groupController.getIds(credential, firstName, lastName);
        if (0 == ids.length) {
            this.error("No se encontraron grupos", "Los datos no existen");
            return;
        }
        SearchDialog searchDialog = new SearchDialog(ids);
        this.message("Selecciona el grupo", searchDialog);
        String groupName = searchDialog.getSelected();
        this.groupComboBox.setSelectedItem(groupName);
    }

    private void onAllExcel(ActionEvent event) {
        Excel excel = new Excel();
        LocalDateTime now = LocalDateTime.now();
        RowList groups = this.groupController.getAll();
        int count = 0;
        for (Row group : groups) {
            String groupName = group.value("name", String.class);
            RowList students = this.studentController.getByGroup(groupName);
            if (!students.empty()) {
                excel.generate(groupName, now);
                count++;
            }
        }
        this.message("Se generaron los ficheros", String.format("Se generaron #%d ficheros", count));
    }

    private void onExcel(ActionEvent event) {
        if (1 > this.groupComboBox.getSelectedIndex()) {
            this.error("No se pudo generar el fichero", "Seleccione un grupo");
            return;
        }
        if (0 == this.studentsTable.getRowCount()) {
            this.error("No se pudo generar el fichero", "El grupo no tiene alumnos");
            return;
        }
        String groupName = this.groupComboBox.getSelectedItem().toString();
        File file = new Excel().generate(groupName, LocalDateTime.now());
        if (file.exists()) {
            this.message("Se genero el fiecho", file.getAbsolutePath());
        }
    }

    private void onQualification(ActionEvent event) {
        QualificationDialog qualificationDialog = new QualificationDialog();
        int confirm = JOptionPane.showConfirmDialog(this, qualificationDialog,
                "Ingresa el numero de practica y la calificacion",
                JOptionPane.OK_CANCEL_OPTION);
        if (JOptionPane.OK_OPTION == confirm) {
            int practiceNumber = qualificationDialog.getPracticeNumber();
            double qualification = qualificationDialog.getQualification();

            String credential = this.credentialLabel.getText();
            Row student = this.studentController.getByCredential(credential);
            long studentId = student.value("student_id", Long.class);

            if (this.qualificationController.set(this.getGroupId(), studentId, practiceNumber, qualification)) {
                this.message("Se agrego la calificacion correctamente",
                        String.format("[%s] Practica: %d, Calificacion: %.1f", credential, practiceNumber, qualification));
                this.loadQualifications(studentId);
            } else {
                this.error("No se pudo agregar el estudiante", "Ocurrio un error al agregar el estudiante");
            }
        }
    }

    private void onStudentRemove(ActionEvent event) {
        ListSelectionModel selectionModel = this.studentsTable.getSelectionModel();
        if (!selectionModel.isSelectionEmpty()) {
            int selectedIndex = selectionModel.getMinSelectionIndex();
            String credential = this.studentsTable.getValueAt(selectedIndex, 0).toString();

            Row student = this.studentController.getByCredential(credential);
            String firstName = student.value("first_name", String.class);
            String lastName = student.value("last_name", String.class);

            boolean confirm = this.confirm("¿Desea continuar?", String.format("Desea remover al estudiante: [%s] %s %s",
                    credential, firstName, lastName));
            if (confirm) {
                boolean remove = this.groupController.removeStudent(this.getGroupId(), student.value("student_id", Long.class));
                if (!remove) {
                    this.error("No se pudo remover al estudiante",
                            String.format("[%s] %s %s", credential, firstName, lastName));
                    return;
                }
                String groupName = this.groupComboBox.getSelectedItem().toString();
                this.groupComboBox.setSelectedItem(groupName);
            }
        }
    }

    private void onStudentDelete(ActionEvent event) {
        String credential = this.credentialField.getText().toLowerCase();

        if (credential.isEmpty()) {
            this.error("No se pudo eliminar el estudiante", "La matricula no es valida");
            return;
        }

        Row student = this.studentController.getByCredential(credential);
        if (null == student) {
            this.error("No se pudo eliminar el estudiante", String.format("La matricula %s no esta registrada", credential));
            return;
        }
        long studentId = student.value("student_id", Long.class);
        String firstName = student.value("first_name", String.class);
        String lastName = student.value("last_name", String.class);
        if (!this.confirm("¿Desea continuar?", String.format("Desea eliminar al estudiante: [%s] %s %s",
                credential, firstName, lastName))) {
            return;
        }
        if (this.studentController.delete(studentId)) {
            this.message("Se elimino al estudiante correctamente",
                    String.format("[%s] %s %s", credential, firstName, lastName));
            String groupName = this.groupComboBox.getSelectedItem().toString();
            this.groupComboBox.setSelectedItem(groupName);
        } else {
            this.error("No se pudo eliminar el estudiante", "Ocurrio un error al eliminar el estudiante");
        }
    }

    private void onStudentSelected(ListSelectionEvent event) {
        int selectedRow = this.studentsTable.getSelectedRow();
        if (-1 == selectedRow) {
            return;
        }

        String credential = this.studentsTable.getValueAt(selectedRow, 0).toString();
        String firstName = this.studentsTable.getValueAt(selectedRow, 1).toString();
        String lastName = this.studentsTable.getValueAt(selectedRow, 2).toString();

        this.credentialLabel.setText(credential);
        this.studentNameLabel.setText(firstName.concat(" ").concat(lastName));

        Row student = this.studentController.getByCredential(credential);
        if (null != student) {
            long studentId = student.value("student_id", Long.class);
            this.loadQualifications(studentId);
        }

        this.toggle(true);
    }

    private void onStudentSubmit(ActionEvent event) {
        String credential = this.credentialField.getText().toLowerCase();
        String firstName = this.firstNameField.getText();
        String lastName = this.lastNameField.getText();

        if (1 > this.groupComboBox.getSelectedIndex()) {
            this.error("No se pudo agregar el estudiante", "Seleccione un grupo");
            return;
        }

        String groupName = this.groupComboBox.getSelectedItem().toString();
        long groupId = this.groupController.getGroupId(groupName);

        Row student = this.studentController.getByCredential(credential);
        long studentId = -1;
        if (null != student) {
            studentId = student.value("student_id", Long.class);
            if (this.groupController.existStudent(groupId, studentId)) {
                this.error("No se pudo agregar el estudiante", "El estudiante ya esta registrado en este grupo");
                return;
            }
        }

        if (Validators.between(1, 7, credential.length())) {
            this.error("No se pudo agregar el estudiante", "La matricula no es valida");
            return;
        }
        if (Validators.between(1, 64, firstName.length())) {
            this.error("No se pudo agregar el estudiante", "El nombre es demaciado largo o muy corto");
            return;
        }
        if (Validators.between(1, 64, lastName.length())) {
            this.error("No se pudo agregar el estudiante", "El apellido es demaciado largo o muy corto");
            return;
        }

        if (null == student) {
            studentId = this.studentController.create(credential, firstName, lastName);
        }

        if (-1 == studentId) {
            this.error("No se pudo agregar el estudiante", "Ocurrio un error al agregar el estudiante");
            return;
        }

        if (this.groupController.addStudent(groupId, studentId)) {
            this.message("Se agrego el estudiante correctamente", String.format("[%s] %s %s", credential, firstName, lastName));
            DefaultTableModel model = (DefaultTableModel) this.studentsTable.getModel();
            model.addRow(new Object[]{credential, firstName, lastName});

            this.credentialField.setText("");
            this.firstNameField.setText("");
            this.lastNameField.setText("");
        } else {
            this.error("No se pudo agregar el estudiante", "Ocurrio un error al agregar el estudiante");
        }
    }

    private void onGroupSubmit(ActionEvent event) {
        String groupName = JOptionPane.showInputDialog(this, "Ingresa el nombre del grupo");
        if (null == groupName) {
            return;
        }
        groupName = groupName.toUpperCase();
        if (Validators.between(1, 32, groupName.length())) {
            this.error("No se pudo agregar el grupo", "El nombre del grupo debe estar entre 1 y 32 caracteres");
            return;
        }
        if (this.groupController.exist(groupName)) {
            this.error("No se pudo agregar el grupo", "El grupo ya existe");
            return;
        }
        if (this.groupController.create(groupName)) {
            this.message("Se agrego el grupo correctamente", String.format("Grupo: %s", groupName));
            this.loadGroups();
            this.groupComboBox.setSelectedItem(groupName);
        } else {
            this.error("No se pudo agregar el grupo", "Ocurrio un error al agregar el grupo");
        }
    }

    private void onGroupDelete(ActionEvent event) {
        String groupName = JOptionPane.showInputDialog(this, "Ingresa el nombre del grupo");
        if (null == groupName) {
            return;
        }
        groupName = groupName.toUpperCase();
        if (!this.groupController.exist(groupName)) {
            this.error("No se pudo eliminar el grupo", "El grupo no existe");
            return;
        }
        if (this.groupController.delete(groupName)) {
            this.message("Se elimino el grupo correctamente", String.format("Grupo: %s", groupName));
            this.loadGroups();
            this.groupComboBox.setSelectedIndex(0);
        } else {
            this.error("No se pudo eliminar el grupo", "Ocurrio un error al eliminar el grupo");
        }
    }

    private void onGroupSelected(ActionEvent event) {
        this.toggle(false);
        DefaultTableModel model = (DefaultTableModel) this.studentsTable.getModel();
        model.setRowCount(0);
        if (1 > this.groupComboBox.getSelectedIndex()) {
            return;
        }
        String groupName = this.groupComboBox.getSelectedItem().toString();
        RowList students = this.studentController.getByGroup(groupName);
        for (Row student : students) {
            model.addRow(new Object[]{
                student.value("credential"), student.value("first_name"), student.value("last_name")
            });
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        studentTablePopMenu = new javax.swing.JPopupMenu();
        removeStudentMenuItem = new javax.swing.JMenuItem();
        studentsPanel = new javax.swing.JScrollPane();
        studentsTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        credentialField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        firstNameField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        lastNameField = new javax.swing.JTextField();
        addStudentButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        addGroupButton = new javax.swing.JButton();
        groupComboBox = new javax.swing.JComboBox();
        qualificationsPanel = new javax.swing.JScrollPane();
        qualificationsTable = new javax.swing.JTable();
        credentialLabel = new javax.swing.JLabel();
        studentNameLabel = new javax.swing.JLabel();
        qualificationButton = new javax.swing.JButton();
        deleteGroupButton = new javax.swing.JButton();
        excelButton = new javax.swing.JButton();
        averageLabel = new javax.swing.JLabel();
        allExcelButton = new javax.swing.JButton();
        searchButton = new javax.swing.JButton();
        deleteStudentButton = new javax.swing.JButton();

        removeStudentMenuItem.setText("Remover");
        studentTablePopMenu.add(removeStudentMenuItem);

        studentsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Matricula", "Nombres", "Apellidos"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        studentsTable.setColumnSelectionAllowed(true);
        studentsTable.getTableHeader().setReorderingAllowed(false);
        studentsPanel.setViewportView(studentsTable);
        studentsTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (studentsTable.getColumnModel().getColumnCount() > 0) {
            studentsTable.getColumnModel().getColumn(0).setResizable(false);
            studentsTable.getColumnModel().getColumn(1).setResizable(false);
            studentsTable.getColumnModel().getColumn(2).setResizable(false);
        }

        jLabel1.setText("Matricula");

        credentialField.setPreferredSize(new java.awt.Dimension(80, 20));

        jLabel2.setText("Nombres");

        firstNameField.setPreferredSize(new java.awt.Dimension(150, 20));

        jLabel3.setText("Apellidos");

        lastNameField.setPreferredSize(new java.awt.Dimension(150, 20));

        addStudentButton.setText("Agregar");

        jLabel4.setText("Grupo");

        addGroupButton.setText("Nuevo grupo");

        groupComboBox.setPreferredSize(new java.awt.Dimension(100, 20));

        qualificationsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Practica #1", "Practica #2", "Practica #3", "Practica #4", "Practica #5", "Practica #6"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        qualificationsTable.setRowSelectionAllowed(false);
        qualificationsTable.getTableHeader().setReorderingAllowed(false);
        qualificationsPanel.setViewportView(qualificationsTable);
        if (qualificationsTable.getColumnModel().getColumnCount() > 0) {
            qualificationsTable.getColumnModel().getColumn(0).setResizable(false);
            qualificationsTable.getColumnModel().getColumn(1).setResizable(false);
            qualificationsTable.getColumnModel().getColumn(2).setResizable(false);
            qualificationsTable.getColumnModel().getColumn(3).setResizable(false);
            qualificationsTable.getColumnModel().getColumn(4).setResizable(false);
            qualificationsTable.getColumnModel().getColumn(5).setResizable(false);
        }

        credentialLabel.setText("{Credential}");

        studentNameLabel.setText("{Student Name}");

        qualificationButton.setText("Calificar");

        deleteGroupButton.setText("Eliminar grupo");

        excelButton.setText("Generar actual");

        averageLabel.setText("{average}");

        allExcelButton.setText("Generar todo");

        searchButton.setText("Buscar");

        deleteStudentButton.setText("Eliminar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(qualificationButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(credentialLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(studentNameLabel))
                            .addComponent(qualificationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 444, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(averageLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(studentsPanel)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(credentialField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(firstNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lastNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(searchButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteStudentButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(groupComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addGroupButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteGroupButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(allExcelButton)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(excelButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(addStudentButton, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(addGroupButton)
                    .addComponent(groupComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteGroupButton)
                    .addComponent(excelButton)
                    .addComponent(allExcelButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(credentialField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(firstNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(lastNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton)
                    .addComponent(deleteStudentButton)
                    .addComponent(addStudentButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(studentsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(credentialLabel)
                    .addComponent(studentNameLabel)
                    .addComponent(qualificationButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qualificationsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(averageLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addGroupButton;
    private javax.swing.JButton addStudentButton;
    private javax.swing.JButton allExcelButton;
    private javax.swing.JLabel averageLabel;
    private javax.swing.JTextField credentialField;
    private javax.swing.JLabel credentialLabel;
    private javax.swing.JButton deleteGroupButton;
    private javax.swing.JButton deleteStudentButton;
    private javax.swing.JButton excelButton;
    private javax.swing.JTextField firstNameField;
    private javax.swing.JComboBox groupComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField lastNameField;
    private javax.swing.JButton qualificationButton;
    private javax.swing.JScrollPane qualificationsPanel;
    private javax.swing.JTable qualificationsTable;
    private javax.swing.JMenuItem removeStudentMenuItem;
    private javax.swing.JButton searchButton;
    private javax.swing.JLabel studentNameLabel;
    private javax.swing.JPopupMenu studentTablePopMenu;
    private javax.swing.JScrollPane studentsPanel;
    private javax.swing.JTable studentsTable;
    // End of variables declaration//GEN-END:variables
}
