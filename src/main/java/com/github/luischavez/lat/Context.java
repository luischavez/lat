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
package com.github.luischavez.lat;

import com.github.luischavez.database.Database;
import com.github.luischavez.lat.controller.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class Context {

    private static final List<Controller> CONTROLLERS = new ArrayList<>();

    public static Database database() {
        return Database.use("default");
    }

    private static <C extends Controller> C newInstance(Class<C> type) {
        C controller;
        try {
            controller = type.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
        return controller;
    }

    public static <C extends Controller> C controller(Class<C> type) {
        for (Controller controller : Context.CONTROLLERS) {
            if (type.isAssignableFrom(controller.getClass())) {
                return type.cast(controller);
            }
        }
        C controller = Context.newInstance(type);
        controller.setDatabase(Context.database());
        Context.CONTROLLERS.add(controller);
        return controller;
    }
}
