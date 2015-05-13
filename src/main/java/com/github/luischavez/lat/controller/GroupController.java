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
import com.github.luischavez.database.query.Query;

import com.github.luischavez.lat.Constants;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class GroupController extends Controller {

    public RowList getAll() {
        return this.database
                .table(Constants.GROUPS_TABLE)
                .get();
    }

    public long[] getIds(String credential, String firstName, String lastName) {
        Query query = this.database.table(Constants.STUDENTS_TABLE);
        if ((null == credential || credential.isEmpty())
                && (null == firstName || firstName.isEmpty())
                && (null == lastName || lastName.isEmpty())) {
            return new long[0];
        }
        if (null != credential && !credential.isEmpty()) {
            query.where("credential", "LIKE", credential);
        }
        if (null != firstName && !firstName.isEmpty()) {
            query.where("first_name", "LIKE", firstName);
        }
        if (null != lastName && !lastName.isEmpty()) {
            query.where("last_name", "LIKE", lastName);
        }
        Row student = query.first("student_id");
        if (null == student) {
            return new long[0];
        }
        RowList groups = this.database.table(Constants.STUDENT_GROUPS_TABLE)
                .where("student_id", "=", student.value("student_id"))
                .get("group_id");
        long[] groupIds = new long[groups.size()];
        for (int i = 0; i < groupIds.length; i++) {
            groupIds[i] = groups.getRow(i).value("group_id", Long.class);
        }
        return groupIds;
    }

    public boolean exist(String groupName) {
        Row group = this.database
                .table(Constants.GROUPS_TABLE)
                .where("name", "=", groupName)
                .first("group_id");
        return null != group;
    }

    public long getGroupId(String groupName) {
        Row group = this.database
                .table(Constants.GROUPS_TABLE)
                .where("name", "=", groupName)
                .first("group_id");
        return null != group ? group.value("group_id", Long.class) : -1;
    }

    public String getName(long groupId) {
        Row group = this.database
                .table(Constants.GROUPS_TABLE)
                .where("group_id", "=", groupId)
                .first("name");
        return null != group ? group.value("name", String.class) : "";
    }

    public boolean create(String groupName) {
        Affecting insert = this.database
                .insert(Constants.GROUPS_TABLE,
                        "name",
                        groupName);
        return insert.success();
    }

    public boolean delete(String groupName) {
        Affecting delete = this.database
                .where("name", "=", groupName)
                .delete(Constants.GROUPS_TABLE);
        return delete.success();
    }

    public boolean existStudent(long groupId, long studentId) {
        Row student = this.database
                .table(Constants.STUDENT_GROUPS_TABLE)
                .where("group_id", "=", groupId)
                .where("student_id", "=", studentId)
                .first();
        return null != student;
    }

    public boolean addStudent(long groupId, long studentId) {
        Affecting insert = this.database
                .insert(Constants.STUDENT_GROUPS_TABLE,
                        "group_id, student_id",
                        groupId, studentId);
        return insert.success();
    }

    public boolean removeStudent(long groupId, long studentId) {
        Affecting delete = this.database
                .where("group_id", "=", groupId)
                .where("student_id", "=", studentId)
                .delete(Constants.STUDENT_GROUPS_TABLE);
        return delete.success();
    }
}
