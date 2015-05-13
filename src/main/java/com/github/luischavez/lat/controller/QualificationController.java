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
package com.github.luischavez.lat.controller;

import com.github.luischavez.database.link.Affecting;
import com.github.luischavez.database.link.Row;
import com.github.luischavez.lat.Constants;

import java.math.BigDecimal;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class QualificationController extends Controller {

    public double get(long groupId, long studentId, int practiceNumber) {
        Row qualification = this.database
                .table(Constants.QUALIFICATIONS_TABLE)
                .where("group_id", "=", groupId)
                .where("student_id", "=", studentId)
                .where("practice_number", "=", practiceNumber)
                .first("qualification");
        if (null == qualification) {
            return -1f;
        }
        return qualification.value("qualification", BigDecimal.class).doubleValue();
    }

    public boolean set(long groupId, long studentId, int practiceNumber, double qualification) {
        Row row = this.database
                .table(Constants.QUALIFICATIONS_TABLE)
                .where("group_id", "=", groupId)
                .where("student_id", "=", studentId)
                .where("practice_number", "=", practiceNumber)
                .first("qualification");
        if (null != row) {
            Affecting update = this.database
                    .where("group_id", "=", groupId)
                    .where("student_id", "=", studentId)
                    .where("practice_number", "=", practiceNumber)
                    .update(Constants.QUALIFICATIONS_TABLE, "qualification", qualification);
            return update.success();
        } else {
            Affecting insert = this.database
                    .insert(Constants.QUALIFICATIONS_TABLE,
                            "group_id, student_id, practice_number, qualification",
                            groupId, studentId, practiceNumber, qualification);
            return insert.success();
        }
    }
}
