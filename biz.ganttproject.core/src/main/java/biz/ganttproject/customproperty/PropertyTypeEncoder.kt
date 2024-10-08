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
package biz.ganttproject.customproperty

import biz.ganttproject.core.time.CalendarFactory
import org.w3c.util.DateParser
import org.w3c.util.InvalidDateException
import java.util.*

typealias DateParserFn = (String) -> Date?
/**
 * @author dbarashev@bardsoftware.com
 */
object PropertyTypeEncoder {
  private var dateParser: DateParserFn = { date ->
    try {
      DateParser.parse(date)
    } catch (ex: InvalidDateException) {
      ex.printStackTrace()
      null
    }
  }

  fun setDateParser(fn: DateParserFn) {
    dateParser = fn
  }

  fun encodeFieldType(fieldType: Class<*>): String? {
    var result: String? = null
    if (fieldType == java.lang.String::class.java) {
      result = "text"
    } else if (fieldType == java.lang.Boolean::class.java) {
      result = "boolean"
    } else if (fieldType == java.lang.Integer::class.java) {
      result = "int"
    } else if (fieldType == java.lang.Double::class.java) {
      result = "double"
    } else if (GregorianCalendar::class.java.isAssignableFrom(fieldType)) {
      result = "date"
    }
    return result
  }

  fun decodeTypeAndDefaultValue(
    typeAsString: String?, valueAsString: String?
  ): CustomPropertyDefinitionStub {
    return when (typeAsString) {
      "text" -> create(CustomPropertyClass.TEXT, valueAsString)
      "boolean" -> create(CustomPropertyClass.BOOLEAN, valueAsString)
      "int" -> create(CustomPropertyClass.INTEGER, valueAsString)
      "integer" -> create(CustomPropertyClass.INTEGER, valueAsString)
      "double" -> create(CustomPropertyClass.DOUBLE, valueAsString)
      "date" -> create(CustomPropertyClass.DATE, valueAsString)
      else -> create(CustomPropertyClass.TEXT, "")
    }
  }

  data class CustomPropertyDefinitionStub(val propertyClass: CustomPropertyClass, val defaultValue: Any?)

  fun create(propertyClass: CustomPropertyClass, valueAsString: String?): CustomPropertyDefinitionStub {
    val defaultValue = when (propertyClass) {
      CustomPropertyClass.TEXT -> valueAsString
      CustomPropertyClass.BOOLEAN -> if (valueAsString == null) null else java.lang.Boolean.valueOf(valueAsString)
      CustomPropertyClass.INTEGER -> {
        try {
          if (valueAsString == null) null else Integer.valueOf(valueAsString)
        } catch (e: NumberFormatException) {
          null
        }
      }
      CustomPropertyClass.DOUBLE -> {
        try {
          if (valueAsString == null) null else java.lang.Double.valueOf(valueAsString)
        } catch (e: NumberFormatException) {
          null
        }
      }
      CustomPropertyClass.DATE -> {
        if (valueAsString.isNullOrBlank()) {
          null
        } else {
          dateParser(valueAsString)?.let { CalendarFactory.createGanttCalendar(it) }
        }
      }
    }
    return CustomPropertyDefinitionStub(propertyClass, defaultValue)
  }
}
