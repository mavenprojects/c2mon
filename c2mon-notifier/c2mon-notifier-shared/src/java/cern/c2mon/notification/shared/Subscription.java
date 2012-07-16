/**
 * 
 */
package cern.c2mon.notification.shared;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Set;

import cern.dmn2.core.Status;

/**
 * @author felixehm
 */
public class Subscription implements Comparable<Subscription> {

    /**
     * 
     */
    private Status lastNotifiedStatus = Status.UNKNOWN;

    /**
     * 
     */
    private Timestamp lastNotification = null;

    /**
     * level to notify for
     */
    private int level = 1;

    /**
     * the user this subscription belongs to.
     */
    private final String user;

    /**
     * the tag id this subscription is valid for.
     */
    private final Long ruleTagId;

    /**
     * enabled / disabled ?
     */
    private boolean isEnabled = true;

    /**
     * array which keeps boolean flags for the different kind of notifications.
     */
    private boolean[] notificationMethods = new boolean[2];

    /**
     * a list which contains information on the last notified tags
     */
    protected HashMap<Long, Status> lastNotifiedTags = new HashMap<Long, Status>();

    /**
     * flag to indicate that this subscription should be used when metric changes happen. 
     */
    private boolean notifyOnMetricChange = false;
    
    /**
     * Holds the client-side rule expression (if desired by the user)
     */
    private String ruleExpression = null;

    /**
     * Constructs a new subscription for a notification.
     * 
     * @param userId the user id of which this Subscription belongs to.
     * @param tagId the tag id [long] this subscription belongs to
     * @param level the notifcation level [int]
     */
    public Subscription(String userId, Long tagId, int level) {
        this.level = level;
        if (userId == null) {
            throw new IllegalArgumentException("Passed argument 'user' was null");
        }

        this.user = userId;
        this.ruleTagId = tagId;

        // We always have notification method 'Mail' :
        notificationMethods[0] = true;
        // but not for SMS
        notificationMethods[1] = false;
    }

    /**
     * Creates a subscription object with notification level WARNING.
     * 
     * @param user the {@link Subscriber} which this Subscription belongs to.
     * @param tagId tagId the tag id [long] this subscription belongs to
     */
    public Subscription(Subscriber user, Long tagId) {
        this(user.getUserName(), tagId, Status.WARNING.ordinal());
    }

    /**
     * Creates a subscription object with notification level WARNING.
     * 
     * @param user the user id of which this Subscription belongs to.
     * @param tagId tagId the tag id [long] this subscription belongs to
     */
    public Subscription(String user, Long tagId) {
        this(user, tagId, Status.WARNING.ordinal());
    }

    /**
     * Creates a subscription object with desired notification level.<br><br>
     * <b>Note:</b> this constructor also adds itself to the passed Subscriber object {@link Subscriber#addSubscription(Subscription)}. No need to do this in a second call.
     * 
     * @param user the {@link Subscriber} which this Subscription belongs to.
     * @param tagId tagId the tag id [long] this subscription belongs to
     * @param level the notification level for this subscription.
     */
    public Subscription(Subscriber user, Long tagId, int level) {
        this(user.getUserName(), tagId, level);
        user.addSubscription(this);
    }

    /**
     * (re-)Enables this subscription.
     * 
     * @param newFlag true or false.
     */
    public void setEnabled(boolean newFlag) {
        isEnabled = newFlag;
    }

    /**
     * @return the notification level.
     */
    public int getNotificationLevel() {
        return level;
    }

    /**
     * Sets the notification level to the desired level
     * @see Status
     * @param level 
     */
    public void setNotificationLevel(int level) {
        Status s = Status.fromInt(level);
        if (s.equals(Status.UNKNOWN) || s.equals(Status.OK)) {
            throw new IllegalArgumentException("The level argument is not allowed to be UNKNOWN or OK.");
        }
        this.level = level;
    }
    
    /**
     * @return true in case this subscription is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * @param toCheck the level to check.
     * @return true if this subscription triggers a notification based on the passed level.
     */
    public boolean isInterestedInLevel(int toCheck) {
        return (toCheck >= level || toCheck == 0) ? true : false;
    }

    /**
     * @return true, in case this subscriptions triggers sending a mail.
     */
    public boolean isMailNotification() {
        return notificationMethods[0];
    }

    /**
     * @return true, in case this subscriptions triggers sending a SMS.
     */
    public boolean isSmsNotification() {
        return notificationMethods[1];
    }

    /**
     * Enables or disables sending Mail notifications for this subscription.
     * 
     * @param newFlag true or false
     */
    public void setMailNotification(boolean newFlag) {
        notificationMethods[0] = newFlag;
    }

    /**
     * Enables or disables sending SMS notifications for this subscription.
     * 
     * @param newFlag true or false
     */
    public void setSmsNotification(boolean newFlag) {
        notificationMethods[1] = newFlag;
    }

    /**
     * @return Returns the notifyOnMetricChange.
     */
    public boolean isNotifyOnMetricChange() {
        return notifyOnMetricChange;
    }

    /**
     * @param notifyOnMetricChange Triggers a notification whenever a metric change appear. <b>Note:</b> This can be very often!
     */
    public void setNotifyOnMetricChange(boolean notifyOnMetricChange) {
        this.notifyOnMetricChange = notifyOnMetricChange;
    }

    /**
     * @return the owner of this subscription.
     */
    public final String getSubscriberId() {
        return user;
    }

    /**
     * @return the id of the tag this subscription is valid for.
     */
    public Long getTagId() {
        return ruleTagId;
    }

    @Override
    public boolean equals(Object o) {
        Subscription s = (Subscription) o;
        return (s.getTagId() == this.getTagId() && s.getNotificationLevel() == this.getNotificationLevel() && s
                .getSubscriberId().equals(this.getSubscriberId()));
    }
    
    @Override
    public int hashCode() {
        return this.getSubscriberId().hashCode() ^ this.getTagId().hashCode();
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("TagId=").append(ruleTagId)
                .append(", User={").append(user)
                .append("}, Level=").append(level)
                .append(", Enabled=").append(isEnabled)
                .append(", LastNotifiedState=").append(lastNotifiedStatus)
                .append(", LastNotifiedTime").append(lastNotification);
        return ret.toString();
    }

    @Override
    public int compareTo(Subscription o) {
        return o.getTagId().compareTo(this.getTagId());
    }

    /**
     * @return an exact copy of this object.
     */
    public Subscription getCopy() {
        Subscription sub = new Subscription(this.getSubscriberId(), this.getTagId(), this.getNotificationLevel());
        sub.setEnabled(this.isEnabled());
        sub.setLastNotification(getLastNotification());
        sub.setLastNotifiedStatus(getLastNotifiedStatus());
        sub.setMailNotification(isMailNotification());
        sub.setSmsNotification(isSmsNotification());
        return sub;
    }

    /**
     * <b>Note:</b> No need to set the timestamp. This is already done for you.
     * 
     * @param newStatus the status as integer representation
     */
    public void setLastNotifiedStatus(Status newStatus) {
        this.lastNotifiedStatus = newStatus;
        setLastNotification(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * @see Subscription#getLastNotification()
     * @return the last notified status as integer.
     */
    public Status getLastNotifiedStatus() {
        return this.lastNotifiedStatus;
    }

    /**
     * @return Returns the lastNotification.
     */
    public Timestamp getLastNotification() {
        return lastNotification;
    }

    /**
     * @param lastNotification The lastNotification to set.
     */
    public void setLastNotification(Timestamp lastNotification) {
        this.lastNotification = lastNotification;
    }

    /**
     * @return a list of tag ids this subscription implicitly spans. This was set in the server and cannot be changed on
     *         the client side.
     */
    public Set<Long> getResolvedSubTagIds() {
        return lastNotifiedTags.keySet();
    }
    
    
    public void addResolvedSubTag(Long l) {
        if (!lastNotifiedTags.containsKey(l)) {
            lastNotifiedTags.put(l, Status.UNKNOWN);
        }
    }
    
    public Status getLastStatusForResolvedSubTag(Long tagId) {
        Status s = lastNotifiedTags.get(tagId);
        if (s != null) {
            return s;
        } else {
            throw new IllegalArgumentException("Rule # " + getTagId() +  ": Tag #" + tagId + " in not in my resolved list.");
        }
    }
    
    
    public void setLastStatusForResolvedTSubTag(Long tagId, Status status) {
        if (!lastNotifiedTags.containsKey(tagId)) {
            throw new IllegalArgumentException("Tag # " + getTagId() + ": I do not have a resolved child with ID=" + tagId);
        }
        lastNotifiedTags.put(tagId, status);
    }
}
