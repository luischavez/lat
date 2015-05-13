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
package com.github.luischavez.lat.excel;

import com.github.luischavez.database.link.RowList;
import com.github.luischavez.lat.Context;
import com.github.luischavez.lat.controller.GroupController;
import com.github.luischavez.lat.controller.QualificationController;
import com.github.luischavez.lat.controller.StudentController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class Excel {

    private final GroupController groupController = Context.controller(GroupController.class);
    private final StudentController studentController = Context.controller(StudentController.class);
    private final QualificationController qualificationController = Context.controller(QualificationController.class);

    protected void createCell(int index, Row row, CellStyle style, String value) {
        Cell cell = row.createCell(index);
        if (null != style) {
            cell.setCellStyle(style);
        }
        cell.setCellValue(value);
    }

    protected void createNumber(int index, Row row, CellStyle style, double value) {
        Cell cell = row.createCell(index);
        if (null != style) {
            cell.setCellStyle(style);
        }
        cell.setCellValue(value);
    }

    protected void createFormula(int index, Row row, CellStyle style, String formula) {
        Cell cell = row.createCell(index);
        if (null != style) {
            cell.setCellStyle(style);
        }
        cell.setCellType(Cell.CELL_TYPE_FORMULA);
        cell.setCellFormula(formula);
    }

    protected void setContent(Workbook workbook, Sheet sheet, String groupName) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(HSSFColor.WHITE.index);
        style.setFont(font);
        style.setFillBackgroundColor(IndexedColors.AQUA.getIndex());
        style.setFillPattern(CellStyle.BIG_SPOTS);

        Row row = sheet.createRow(0);
        this.createCell(0, row, style, "#");
        this.createCell(1, row, style, "Matricula");
        this.createCell(2, row, style, "Nombres");
        this.createCell(3, row, style, "Apellidos");
        for (int i = 0; i < 6; i++) {
            this.createCell(4 + i, row, style, "Practica #" + (i + 1));
        }
        this.createCell(10, row, style, "Promedio");

        int current = 1;
        RowList students = this.studentController.getByGroup(groupName);
        long groupId = this.groupController.getGroupId(groupName);
        for (com.github.luischavez.database.link.Row student : students) {
            long studentId = student.value("student_id", Long.class);
            String credential = student.value("credential", String.class);
            String firstName = student.value("first_name", String.class);
            String lastName = student.value("last_name", String.class);
            row = sheet.createRow(current++);
            this.createNumber(0, row, null, current);
            this.createCell(1, row, null, credential);
            this.createCell(2, row, null, firstName);
            this.createCell(3, row, null, lastName);
            for (int i = 0; i < 6; i++) {
                double qualification = this.qualificationController.get(groupId, studentId, i + 1);
                if (0 > qualification) {
                    qualification = 0;
                }
                this.createNumber(4 + i, row, null, qualification);
            }
            this.createFormula(10, row, style, String.format("AVERAGE(E%d:J%d)", current, current));
        }
        row = sheet.createRow(current);
        this.createFormula(10, row, style, String.format("AVERAGE(K%d:K%d)", 2, current));
    }

    public File generate(String groupName, LocalDateTime dateTime) {
        String datetime = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .replaceAll("-", "_")
                .replaceAll(":", "_")
                .replaceAll("\\.", "_");
        String path = System.getProperty("user.dir")
                .concat("/excel/")
                .concat(datetime).concat("/");

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(path.concat(groupName).concat(".xls"));

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(groupName);
        this.setContent(workbook, sheet, groupName);
        for (int i = 0; i < 11; i++) {
            sheet.autoSizeColumn(i);
        }
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return file;
    }
}
