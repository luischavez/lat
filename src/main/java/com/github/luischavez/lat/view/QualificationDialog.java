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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class QualificationDialog extends JPanel {

    private final JSpinner practiceNumberSpinner;
    private final JSpinner qualificationSpinner;

    public QualificationDialog() {
        this.practiceNumberSpinner = new JSpinner();
        this.qualificationSpinner = new JSpinner();

        this.practiceNumberSpinner.setModel(new SpinnerNumberModel(1, 1, 6, 1));
        this.qualificationSpinner.setModel(new SpinnerNumberModel(5, 0, 10, 0.1));

        this.add(new JLabel("Practica #"));
        this.add(this.practiceNumberSpinner);
        this.add(new JLabel("Calificacion"));
        this.add(this.qualificationSpinner);
    }

    public int getPracticeNumber() {
        return (Integer) this.practiceNumberSpinner.getValue();
    }

    public double getQualification() {
        return (Double) this.qualificationSpinner.getValue();
    }
}
