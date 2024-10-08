/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2011 Dmitry Barashev

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.gui;

import biz.ganttproject.lib.fx.DesktopKt;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.List;

public interface NotificationManager {
  NotificationItem createNotification(NotificationChannel channel, String title, String body, HyperlinkListener hyperlinkListener);

  void addNotifications(List<NotificationItem> notifications);

  void showNotification(NotificationChannel channel);

  HyperlinkListener DEFAULT_HYPERLINK_LISTENER = hyperlinkEvent -> {
    if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      DesktopKt.openInBrowser(hyperlinkEvent.getURL().toString());
    }
  };
}
