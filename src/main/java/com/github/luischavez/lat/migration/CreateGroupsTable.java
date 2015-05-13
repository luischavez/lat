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
public class CreateGroupsTable implements Migration {

    @Override
    public void up(Database database) {
        database.create("groups", table -> {
            table.integer("group_id").incremented();
            table.string("name", 32);

            table.primary("group_id");
            table.unique("name");
        });
    }

    @Override
    public void down(Database database) {
        database.drop("groups");
    }
}
