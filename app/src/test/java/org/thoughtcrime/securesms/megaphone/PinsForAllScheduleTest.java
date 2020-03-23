package org.thoughtcrime.securesms.megaphone;

import android.app.Application;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.keyvalue.KbsValues;
import org.thoughtcrime.securesms.keyvalue.RegistrationValues;
import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.util.FeatureFlags;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApplicationDependencies.class, SignalStore.class, FeatureFlags.class, RegistrationValues.class, KbsValues.class, TextSecurePreferences.class})
public class PinsForAllScheduleTest {

  private final PinsForAllSchedule testSubject        = new PinsForAllSchedule();
  private final RegistrationValues registrationValues = mock(RegistrationValues.class);
  private final KbsValues          kbsValues          = mock(KbsValues.class);

  @Before
  public void setUp() {
    mockStatic(ApplicationDependencies.class);
    mockStatic(SignalStore.class);
    mockStatic(FeatureFlags.class);
    mockStatic(TextSecurePreferences.class);
    when(ApplicationDependencies.getApplication()).thenReturn(mock(Application.class));
    when(SignalStore.registrationValues()).thenReturn(registrationValues);
    when(SignalStore.kbsValues()).thenReturn(kbsValues);
    when(TextSecurePreferences.isV1RegistrationLockEnabled(any())).thenReturn(false);
  }

  @Test
  public void givenFirstVisibleIsZero_whenIShouldDisplayFullscreen_thenIExpectFalse() {
    // GIVEN
    long firstVisible = 0;

    // WHEN
    boolean result = PinsForAllSchedule.shouldDisplayFullScreen(firstVisible, 0);

    // THEN
    assertFalse(result);
  }

  @Test
  public void givenFirstVisibleIsNow_whenIShouldDisplayFullscreen_thenIExpectFalse() {
    // GIVEN
    long now = System.currentTimeMillis();

    // WHEN
    boolean result = PinsForAllSchedule.shouldDisplayFullScreen(now, now);

    // THEN
    assertFalse(result);
  }

  @Test
  public void givenFirstVisibleIsHalfFullscreenTimeout_whenIShouldDisplayFullscreen_thenIExpectFalse() {
    // GIVEN
    long now      = System.currentTimeMillis();
    long lastWeek = now - TimeUnit.DAYS.toMillis(PinsForAllSchedule.DAYS_UNTIL_FULLSCREEN / 2);

    // WHEN
    boolean result = PinsForAllSchedule.shouldDisplayFullScreen(lastWeek, now);

    // THEN
    assertFalse(result);
  }


  // TODO [greyson]
  @Ignore
  @Test
  public void givenFirstVisibleIsFullscreenTimeout_whenIShouldDisplayFullscreen_thenIExpectTrue() {
    // GIVEN
    long now      = System.currentTimeMillis();
    long lastWeek = now - TimeUnit.DAYS.toMillis(PinsForAllSchedule.DAYS_UNTIL_FULLSCREEN);

    // WHEN
    boolean result = PinsForAllSchedule.shouldDisplayFullScreen(lastWeek, now);

    // THEN
    assertTrue(result);
  }

  @Test
  public void givenFirstVisibleIsZero_whenIGetDaysRemaining_thenIExpectMax() {
    // GIVEN
    long firstVisible = 0;
    long expected     = PinsForAllSchedule.DAYS_REMAINING_MAX;

    // WHEN
    long result = PinsForAllSchedule.getDaysRemaining(firstVisible, 0);

    // THEN
    assertEquals(expected, result);
  }

  @Test
  public void givenFirstVisibleIsNow_whenIGetDaysRemaining_thenIExpectMax() {
    // GIVEN
    long now      = System.currentTimeMillis();
    long expected = PinsForAllSchedule.DAYS_REMAINING_MAX;

    // WHEN
    long result = PinsForAllSchedule.getDaysRemaining(now, now);

    // THEN
    assertEquals(expected, result);
  }

  @Test
  public void givenFirstVisibleIsFiveSecondsAgo_whenIGetDaysRemaining_thenIExpectMax() {
    // GIVEN
    long now          = System.currentTimeMillis();
    long firstVisible = now - TimeUnit.SECONDS.toMillis(5);
    long expected     = PinsForAllSchedule.DAYS_REMAINING_MAX;

    // WHEN
    long result = PinsForAllSchedule.getDaysRemaining(firstVisible, now);

    // THEN
    assertEquals(expected, result);
  }

  @Test
  public void givenFirstVisibleIsADayAgo_whenIGetDaysRemaining_thenIExpectMaxLessOne() {
    // GIVEN
    long now          = System.currentTimeMillis();
    long firstVisible = now - TimeUnit.DAYS.toMillis(1);
    long expected     = PinsForAllSchedule.DAYS_REMAINING_MAX - 1;

    // WHEN
    long result = PinsForAllSchedule.getDaysRemaining(firstVisible, now);

    // THEN
    assertEquals(expected, result);
  }

  @Test
  public void givenFirstVisibleIsAMonthAgo_whenIGetDaysRemaining_thenIExpectZero() {
    // GIVEN
    long now          = System.currentTimeMillis();
    long firstVisible = now - TimeUnit.DAYS.toMillis(31);
    long expected     = 0;

    // WHEN
    long result = PinsForAllSchedule.getDaysRemaining(firstVisible, now);

    // THEN
    assertEquals(expected, result);
  }

  @Test
  public void whenUserIsANewInstallAndFlagIsDisabled_whenIShouldDisplay_thenIExpectFalse() {
    // GIVEN
    when(registrationValues.pinWasRequiredAtRegistration()).thenReturn(true);
    when(kbsValues.isV2RegistrationLockEnabled()).thenReturn(true);
    when(FeatureFlags.pinsForAll()).thenReturn(false);

    // WHEN
    boolean result = testSubject.shouldDisplay(0, 0, 0, System.currentTimeMillis());

    // THEN
    assertFalse(result);
  }

  @Test
  public void whenUserIsANewInstallAndFlagIsEnabled_whenIShouldDisplay_thenIExpectFalse() {
    // GIVEN
    when(registrationValues.pinWasRequiredAtRegistration()).thenReturn(true);
    when(kbsValues.isV2RegistrationLockEnabled()).thenReturn(true);
    when(FeatureFlags.pinsForAll()).thenReturn(true);

    // WHEN
    boolean result = testSubject.shouldDisplay(0, 0, 0, System.currentTimeMillis());

    // THEN
    assertFalse(result);
  }

  @Test
  public void whenUserIsNotANewInstallAndFlagIsEnabled_whenIShouldDisplay_thenIExpectTrue() {
    // GIVEN
    when(registrationValues.pinWasRequiredAtRegistration()).thenReturn(false);
    when(FeatureFlags.pinsForAll()).thenReturn(true);

    // WHEN
    boolean result = testSubject.shouldDisplay(0, 0, 0, System.currentTimeMillis());

    // THEN
    assertTrue(result);
  }

  @Test
  public void whenUserIsNotANewInstallAndFlagIsNotEnabled_whenIShouldDisplay_thenIExpectFalse() {
    // GIVEN
    when(registrationValues.pinWasRequiredAtRegistration()).thenReturn(false);
    when(FeatureFlags.pinsForAll()).thenReturn(false);

    // WHEN
    boolean result = testSubject.shouldDisplay(0, 0, 0, System.currentTimeMillis());

    // THEN
    assertFalse(result);
  }

  @Test
  public void whenKillSwitchEnabled_whenIShouldDisplay_thenIExpectFalse() {
    // GIVEN
    when(registrationValues.pinWasRequiredAtRegistration()).thenReturn(false);
    when(FeatureFlags.pinsForAll()).thenReturn(true);
    when(FeatureFlags.pinsForAllMegaphoneKillSwitch()).thenReturn(true);

    // WHEN
    boolean result = testSubject.shouldDisplay(0, 0, 0, System.currentTimeMillis());

    // THEN
    assertFalse(result);
  }
}