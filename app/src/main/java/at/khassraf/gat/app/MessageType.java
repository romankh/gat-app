/**
 * Copyright (C) 2016 Roman Khassraf.
 *
 * This file is part of GAT-App.
 *
 *  GAT-App is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  GAT-App is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GAT-App.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package at.khassraf.gat.app;

public enum MessageType {

    SMS(0),
    SMS_DELIVERY_REPORT(1),
    CLASS0(2),
    CLASS0_DELIVERY_REPORT(3),
    SILENT_TYPE0(4),
    SILENT_TYPE0_DELIVERY_REPORT(5),
    MWIA(6),
    MWID(7),
    MWID_DELIVERY_REPORT(8);

    private int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public boolean hasDeliveryReport() {
        switch (this) {
            case SMS_DELIVERY_REPORT:
            case CLASS0_DELIVERY_REPORT:
            case SILENT_TYPE0_DELIVERY_REPORT:
            case MWID_DELIVERY_REPORT:
                return true;
            default:
                break;
        }
        return false;
    }

    public boolean hasText() {
        switch (this) {
            case SMS:
            case SMS_DELIVERY_REPORT:
            case CLASS0:
            case CLASS0_DELIVERY_REPORT:
                return true;
            default:
                break;
        }
        return false;
    }
}
