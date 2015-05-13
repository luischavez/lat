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
import com.github.luischavez.database.link.RowList;

import com.github.luischavez.lat.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class StudentController extends Controller {

    public boolean exist(String credential) {
        Row student = this.database
                .table(Constants.STUDENTS_TABLE)
                .where("credential", "=", credential)
                .first();
        return null != student;
    }

    public Row getByCredential(String credential) {
        return this.database
                .table(Constants.STUDENTS_TABLE)
                .where("credential", "=", credential)
                .first();
    }

    public Row getByFulltName(String firstName, String lastName) {
        return this.database
                .table(Constants.STUDENTS_TABLE)
                .where("first_name", "=", firstName)
                .where("last_name", "=", lastName)
                .first();
    }

    public RowList getByGroup(String groupName) {
        Row group = this.database
                .table(Constants.GROUPS_TABLE)
                .where("name", "=", groupName)
                .first("group_id");
        if (null != group) {
            Long groupId = group.value("group_id", Long.class);
            RowList studentGroups = this.database
                    .table(Constants.STUDENT_GROUPS_TABLE).
                    where("group_id", "=", groupId)
                    .get("student_id");
            List<Long> studentIds = new ArrayList<>();
            for (Row studentGroup : studentGroups) {
                studentIds.add(studentGroup.value("student_id", Long.class));
            }
            return this.database
                    .table(Constants.STUDENTS_TABLE)
                    .where("student_id", "IN", studentIds.toArray())
                    .get();
        }
        return null;
    }

    public long create(String credential, String firstName, String lastName) {
        Affecting insert = this.database
                .insert(Constants.STUDENTS_TABLE,
                        "credential, first_name, last_name",
                        credential, firstName, lastName);
        return insert.success() ? (Integer) (insert.getGeneratedKeys()[0]) : -1;
    }

    public boolean update(long studentId, String credential, String firstName, String lastName) {
        Affecting update = this.database
                .where("student_id", "=", studentId)
                .update(Constants.STUDENTS_TABLE,
                        "credential, first_name, last_name",
                        credential, firstName, lastName);
        return update.success();
    }

    public boolean delete(long studentId) {
        Affecting delete = this.database
                .where("student_id", "=", studentId)
                .delete(Constants.STUDENTS_TABLE);
        return delete.success();
    }
}
