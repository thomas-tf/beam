package cz.seznam.euphoria.core.client.triggers;

import cz.seznam.euphoria.core.client.dataset.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Trigger} that is periodically fired based on given time interval.
 */
public class PeriodicTimeTrigger implements Trigger {

  private static final Logger LOG = LoggerFactory.getLogger(PeriodicTimeTrigger.class);

  private final long interval;
  private final long lastFireTime;

  public PeriodicTimeTrigger(long interval, long lastFireTime) {
    this.interval = interval;
    this.lastFireTime = lastFireTime;
  }

  @Override
  public TriggerResult init(Window w, TriggerContext ctx) {
    long now = ctx.getCurrentTimestamp();
    long start = now - (now + interval) % interval;

    if (scheduleNext(start, w, ctx)) {
      return TriggerResult.CONTINUE;
    }

    return TriggerResult.PASSED;
  }

  @Override
  public TriggerResult onTimeEvent(long time, Window w, TriggerContext ctx) {
    LOG.debug("Firing PeriodicTimeTrigger, time {}, window: {}", time, w.getLabel());

    // ~ reschedule the trigger
    scheduleNext(time, w, ctx);

    return TriggerResult.FLUSH;
  }

  /**
   * @return {@code false} when end of window reached
   */
  private boolean scheduleNext(long currentTime, Window w, TriggerContext ctx) {
    long fire = currentTime;
    while ((fire += interval) <= lastFireTime) {
      if (ctx.scheduleTriggerAt(fire, w, this)) {
        return true;
      }
    }

    return false;
  }
}
