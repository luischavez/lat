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
import com.github.luischavez.database.configuration.ProjectSource;
import com.github.luischavez.database.configuration.XMLBuilder;

import com.github.luischavez.lat.view.Window;
import com.github.luischavez.lat.view.Splash;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class Main {

    public static final String[] AUTHORS = {
        "Luis Abdul Chávez Bustamante",
        "Javier Maldonado Rivera",
        "Cristian Manuel Franco Diaz",
        "Cesar Omar Hernandez Casas",
        "Rubí Ozeta Zavala"
    };

    public static void main(String[] args) {
        Database.load(new XMLBuilder(), new ProjectSource("/database.xml"));
        Database database = Database.use("default");
        database.open();
        database.migrate();

        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.luna.LunaLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            throw new RuntimeException(ex);
        }
        new Splash(AUTHORS).show(() -> new Window());
    }
}
