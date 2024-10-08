/*
 * Copyright 2024 BarD Software s.r.o., Dmitry Barashev.
 *
 * This file is part of GanttProject, an opensource project management tool.
 *
 * GanttProject is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * GanttProject is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */
package biz.ganttproject.core.time

import biz.ganttproject.core.time.TimeTestHelper.initLocale
import biz.ganttproject.core.time.TimeTestHelper.newFriday
import biz.ganttproject.core.time.TimeTestHelper.newMonday
import biz.ganttproject.core.time.impl.GPTimeUnitStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GPTimeUnitStackTest {
  @BeforeEach
  fun setUp() {
    initLocale()
  }

  @Test
  fun `calculate duration in days`() {
    assertEquals(3, GPTimeUnitStack.DAY.duration(newFriday().time, newMonday().time).length)
    assertEquals(-3, GPTimeUnitStack.DAY.duration(newMonday().time, newFriday().time).length)
  }
}