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
package com.github.luischavez.lat.migration;

import com.github.luischavez.database.Database;
import com.github.luischavez.database.Migration;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class CreateQualificationsTable implements Migration {

    @Override
    public void up(Database database) {
        database.create("qualifications", table -> {
            table.integer("qualification_id").incremented();
            table.integer("group_id");
            table.integer("student_id");
            table.integer("practice_number");
            table.decimal("qualification", 3, 2);

            table.primary("qualification_id");
            table.unique("group_id, student_id, practice_number");
            table.foreign("group_id", "groups", "group_id", "CASCADE", "CASCADE");
            table.foreign("student_id", "students", "student_id", "CASCADE", "CASCADE");
        });
    }

    @Override
    public void down(Database database) {
        database.drop("qualifications");
    }
}
