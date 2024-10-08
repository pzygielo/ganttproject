/*
GanttProject is an opensource project management tool.
Copyright (C) 2011-2012 GanttProject Team

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
package net.sourceforge.ganttproject.export;

import biz.ganttproject.core.option.*;
import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.GanttExportSettings;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.language.GanttLanguage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.prefs.Preferences;
import org.w3c.util.DateParser;
import org.w3c.util.InvalidDateException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ExporterBase implements Exporter {
  private IGanttProject myProject;
  private Chart myGanttChart;
  private Chart myResourceChart;
  private UIFacade myUIFacade;
  private Preferences myRootPreferences;
  private DefaultDateOption myExportRangeStart;
  private DefaultDateOption myExportRangeEnd;

  protected static final GanttLanguage language = GanttLanguage.getInstance();

  @Override
  public void setContext(IGanttProject project, UIFacade uiFacade, Preferences prefs) {
    myGanttChart = uiFacade.getGanttChart();
    myResourceChart = uiFacade.getResourceChart();
    myProject = project;
    myUIFacade = uiFacade;
    myRootPreferences = prefs;
    Preferences prefNode = prefs.node("/instance/net.sourceforge.ganttproject/export");
    myExportRangeStart = new DefaultDateOption("export.range.start", myGanttChart.getStartDate());
    myExportRangeStart.loadPersistentValue(prefNode.get(
        "export-range-start", DateParser.getIsoDate(myGanttChart.getStartDate())));
    myExportRangeStart.addChangeValueListener(event -> {
      prefNode.put("export-range-start", myExportRangeStart.getPersistentValue());
    });
    myExportRangeStart.setValueValidator(date -> {
      if (date.after(myExportRangeEnd.getValue())) {
        return new kotlin.Pair(false, "Start date > end date");
      } else {
        return new kotlin.Pair(true, "");
      }
    });
    myExportRangeEnd = new DefaultDateOption("export.range.end", myGanttChart.getEndDate());
    myExportRangeEnd.loadPersistentValue(prefNode.get(
        "export-range-end", DateParser.getIsoDate(myGanttChart.getEndDate())));
    myExportRangeEnd.setValueValidator(date -> {
      if (date.before(myExportRangeStart.getValue())) {
        return new kotlin.Pair(false, "Start date > end date");
      } else {
        return new kotlin.Pair(true, "");
      }
    });
    myExportRangeEnd.addChangeValueListener(event -> {
      prefNode.put("export-range-end", myExportRangeEnd.getPersistentValue());
    });
  }

  protected DefaultDateOption getExportRangeStartOption() {
    return myExportRangeStart;
  }

  protected DefaultDateOption getExportRangeEndOption() {
    return myExportRangeEnd;
  }

  protected GPOptionGroup createExportRangeOptionGroup() {
    return new GPOptionGroup("export.range", getExportRangeStartOption(), getExportRangeEndOption());
  }

  public UIFacade getUIFacade() {
    return myUIFacade;
  }

  public IGanttProject getProject() {
    return myProject;
  }

  protected Preferences getPreferences() {
    return myRootPreferences;
  }

  protected Chart getGanttChart() {
    return myGanttChart;
  }

  protected Chart getResourceChart() {
    return myResourceChart;
  }

  @Override
  public @Nullable Exporter withFormat(String format) {
    if (Arrays.asList(getFileExtensions()).contains(format)) {
      setFormat(format);
      return this;
    } else {
      return null;
    }
  }

  protected void setFormat(String format) {}

  //  @Override
//  public String[] getCommandLineKeys() {
//    // By default use the same
//    return getFileExtensions();
//  }

  public GanttExportSettings createExportSettings() {
    GanttExportSettings result = new GanttExportSettings();
    if (myRootPreferences != null) {
      String exportRange = myRootPreferences.get("exportRange", null);
      if (exportRange == null) {
        result.setStartDate(myExportRangeStart.getValue());
        result.setEndDate(myExportRangeEnd.getValue());
      } else {
        String[] rangeBounds = exportRange.split(" ");

        try {
          result.setStartDate(DateParser.parse(rangeBounds[0]));
          result.setEndDate(DateParser.parse(rangeBounds[1]));
        } catch (InvalidDateException e) {
          GPLogger.log(e);
        }
      }
      if (result.getStartDate().after(result.getEndDate())) {
        GPLogger.log(new ValidationException("In the export range the start date=" + result.getStartDate() + " is after the end date=" + result.getEndDate()));
      }
      result.setCommandLineMode(myRootPreferences.getBoolean("commandLine", false));
      if (myRootPreferences.getBoolean("expandResources", false)) {
        result.setExpandedResources("");
      }
    }
    return result;
  }

  @Override
  public void run(final File outputFile, final ExportFinalizationJob finalizationJob) throws Exception {
    //final IJobManager jobManager = Job.getJobManager();
    final List<File> resultFiles = new ArrayList<>();

    var jobs = new ArrayList<>(Arrays.asList(createJobs(outputFile, resultFiles)));
    jobs.add(new ExporterJob("Finalizing") {
      @Override
      protected IStatus run() {
        finalizationJob.run(resultFiles.toArray(new File[0]));
        return Status.OK_STATUS;
      }
    });
    ExporterBackgroundJobsKt.export(jobs, new JobMonitorDialogFx<>(this.getFileTypeDescription(), jobs.size()));
//
//    final IProgressMonitor monitor = jobManager.createProgressGroup();
//    Job driverJob = new Job("Running export") {
//      @Override
//      protected IStatus run(IProgressMonitor monitor) {
//        monitor.beginTask("Running export", jobs.size());
//        for (ExporterJob job : jobs) {
//          if (monitor.isCanceled()) {
//            jobManager.cancel(EXPORT_JOB_FAMILY);
//            return Status.CANCEL_STATUS;
//          }
//          monitor.subTask(job.getName());
//          final IStatus state;
//          try {
//            state = job.run();
//          } catch (Throwable e) {
//            GPLogger.log(new RuntimeException("Export failure. Failed subtask: " + job.getName(), e));
//            monitor.setCanceled(true);
//            continue;
//          }
//
//          if (state.isOK() == false) {
//            GPLogger.log(new RuntimeException("Export failure. Failed subtask: " + job.getName(), state.getException()));
//            monitor.setCanceled(true);
//            continue;
//          }
//          // Sub task for export is finished without problems
//          // So, updated the total amount of work with the current
//          // work performed
//          monitor.worked(1);
//          // and remove the sub task description
//          // (convenient for debugging to know the sub task is
//          // finished properly)
//          monitor.subTask(null);
//        }
//        monitor.done();
//        return Status.OK_STATUS;
//      }
//    };
//    driverJob.setProgressGroup(monitor, 0);
//    driverJob.schedule();
  }

  /** @return a list with {@link ExporterJob}s required to actually export the current format */
  protected abstract ExporterJob[] createJobs(File outputFile, List<File> resultFiles);

  public abstract static class ExporterJob {
    private final String myName;

    protected ExporterJob(String name) {
      myName = name;
    }

    String getName() {
      return myName;
    }

    protected abstract IStatus run();
  }

}
