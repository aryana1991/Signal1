package org.thoughtcrime.securesms.logsubmit;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.util.ByteUnit;
import org.thoughtcrime.securesms.util.ServiceUtil;
import org.thoughtcrime.securesms.util.TextSecurePreferences;
import org.thoughtcrime.securesms.util.Util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class LogSectionSystemInfo implements LogSection {

  @Override
  public @NonNull String getTitle() {
    return "SYSINFO";
  }

  @Override
  public @NonNull CharSequence getContent(@NonNull Context context) {
    final PackageManager pm      = context.getPackageManager();
    final StringBuilder  builder = new StringBuilder();

    builder.append("Time         : ").append(System.currentTimeMillis()).append('\n');
    builder.append("Device       : ").append(Build.MANUFACTURER).append(" ")
                                     .append(Build.MODEL).append(" (")
                                     .append(Build.PRODUCT).append(")\n");
    builder.append("Android      : ").append(Build.VERSION.RELEASE).append(" (")
                                     .append(Build.VERSION.INCREMENTAL).append(", ")
                                     .append(Build.DISPLAY).append(")\n");
    builder.append("ABIs         : ").append(TextUtils.join(", ", getSupportedAbis())).append("\n");
    builder.append("Memory       : ").append(getMemoryUsage()).append("\n");
    builder.append("Memclass     : ").append(getMemoryClass(context)).append("\n");
    builder.append("OS Host      : ").append(Build.HOST).append("\n");
    builder.append("First Version: ").append(TextSecurePreferences.getFirstInstallVersion(context)).append("\n");
    builder.append("App          : ");
    try {
      builder.append(pm.getApplicationLabel(pm.getApplicationInfo(context.getPackageName(), 0)))
             .append(" ")
             .append(pm.getPackageInfo(context.getPackageName(), 0).versionName)
             .append(" (")
             .append(Util.getManifestApkVersion(context))
             .append(")\n");
    } catch (PackageManager.NameNotFoundException nnfe) {
      builder.append("Unknown\n");
    }

    return builder;
  }

  private static @NonNull String getMemoryUsage() {
    Runtime info        = Runtime.getRuntime();
    long    totalMemory = info.totalMemory();

    return String.format(Locale.ENGLISH,
                         "%dM (%.2f%% free, %dM max)",
                         ByteUnit.BYTES.toMegabytes(totalMemory),
                         (float) info.freeMemory() / totalMemory * 100f,
                         ByteUnit.BYTES.toMegabytes(info.maxMemory()));
  }

  private static @NonNull String getMemoryClass(Context context) {
    ActivityManager activityManager = ServiceUtil.getActivityManager(context);
    String          lowMem          = "";

    if (activityManager.isLowRamDevice()) {
      lowMem = ", low-mem device";
    }

    return activityManager.getMemoryClass() + lowMem;
  }

  private static @NonNull Iterable<String> getSupportedAbis() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return Arrays.asList(Build.SUPPORTED_ABIS);
    } else {
      LinkedList<String> abis = new LinkedList<>();
      abis.add(Build.CPU_ABI);
      if (Build.CPU_ABI2 != null && !"unknown".equals(Build.CPU_ABI2)) {
        abis.add(Build.CPU_ABI2);
      }
      return abis;
    }
  }
}
