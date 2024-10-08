/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2011 Dmitry Barashev, GanttProject Team

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
package biz.ganttproject.core.option;

import java.util.*;
import java.util.function.Function;

public class DefaultEnumerationOption<T> extends GPAbstractOption<String> implements EnumerationOption {
  private final List<String> myValues;
  private final Map<String, T> myStringValue_ObjectValue = new LinkedHashMap<>();
  private Function<String, String> myValueLocalizer = null;

  public DefaultEnumerationOption(String id, T[] values) {
    super(id);
    myValues = new ArrayList<>();
    reloadValues(Arrays.asList(values));
  }

  protected void reloadValues(List<T> values) {
    List<String> oldValues = new ArrayList<>(myValues);
    myValues.clear();
    myStringValue_ObjectValue.clear();
    for (T value : values) {
      myStringValue_ObjectValue.put(objectToString(value), value);
    }
    myValues.addAll(myStringValue_ObjectValue.keySet());
    getPropertyChangeSupport().firePropertyChange(EnumerationOption.VALUE_SET, oldValues, myValues);
  }

  protected String objectToString(T obj) {
    assert obj != null;
    return obj.toString();
  }

  protected T stringToObject(String value) {
    if (myStringValue_ObjectValue.isEmpty()) {
      return null;
    }
    return myStringValue_ObjectValue.get(value);
  }

  @Override
  public String[] getAvailableValues() {
    return myValues.toArray(new String[0]);
  }

  @Override
  public String getPersistentValue() {
    return getValue();
  }

  @Override
  public void loadPersistentValue(String value) {
    setValue(value);
  }

  public T getSelectedValue() {
    return stringToObject(getValue());
  }

  public void setSelectedValue(T value) {
    if (value == null) {
      setValue(null);
      return;
    }
    String stringValue = objectToString(value);
    if (myStringValue_ObjectValue.containsKey(stringValue)) {
      setValue(stringValue);
    }
  }

  protected List<T> getTypedValues() {
    return myStringValue_ObjectValue.values().stream().toList();
  }

  @Override
  public void setValueLocalizer(Function<String, String> localizer) {
    myValueLocalizer = localizer;
  }

  @Override
  public Function<String, String> getValueLocalizer() {
    return myValueLocalizer;
  }
}