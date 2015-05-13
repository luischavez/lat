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

import com.github.luischavez.lat.Context;
import com.github.luischavez.lat.controller.GroupController;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Luis Chávez {@literal <https://github.com/luischavez>}
 */
public class SearchDialog extends JPanel {

    private final GroupController groupController = Context.controller(GroupController.class);

    private final JComboBox<String> groupsComboBox;

    public SearchDialog(long[] groupIds) {
        this.groupsComboBox = new JComboBox<>();
        for (int i = 0; i < groupIds.length; i++) {
            String name = this.groupController.getName(groupIds[i]);
            this.groupsComboBox.addItem(name);
        }
        this.add(new JLabel("Grupos"));
        this.add(this.groupsComboBox);
    }

    public String getSelected() {
        return this.groupsComboBox.getSelectedItem().toString();
    }
}
