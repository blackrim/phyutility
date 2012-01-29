package jebl.util;

import org.virion.jam.util.SimpleListener;

import java.awt.*;

/**
 * @author Matt Kearse
 *
 * @version $Id: ProgressListener.java 721 2007-06-05 20:42:51Z matt_kearse $
 *
 * ProgressListener guarantees the following contract:
 *
 *   A call to any of the methods setProgress(), setMessage(), isCanceled() and
 *   setIndeterminateProgress() at a given time yields the same result as a call
 *   to another of these methods would have resulted at the same time.
 *
 *   Once the task whose progress we are observing has been canceled, calls
 *   to either of these methods reflect this. This does not prevent subclasses
 *   from introducing a way to "reset" a ProgressListener that was previously
 *   canceled from not being canceled any more.
 *
 * Any object may exhibit undefined behaviour when dealing with a ProgressListener 
 * that is not fulfilling this contract.
 */
public abstract class ProgressListener {
    /**
     * @param fractionCompleted a number between 0 and 1 inclusive
     * representing the fraction of the operation completed.
     * If you are unsure of the fraction completed, call {@link #setIndeterminateProgress} instead.
     *
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setProgress(double fractionCompleted) {
        _setProgress(fractionCompleted);
        return isCanceled();
    }

    /**
     * This method is a hook called from {@link #setProgress} to allow subclasses a
     * custom reaction to setProgress events. Currently, subclasses are required to
     * implement this method, but in the future it may get an empty default
     * implementation to make it optional for subclasses to subscribe to setProgress
     * events.
     */
    protected abstract void _setProgress(double fractionCompleted);

    /**
     * Sets indefinite progress (i.e. "some progress has happened, but I don't
     * know how close we are to finishing").
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setIndeterminateProgress() {
        _setIndeterminateProgress();
        return isCanceled();
    }

    /**
     * This method is a hook called from {@link #setIndeterminateProgress} to
     * allow subclasses a custom reaction to setIndeterminateProgress events.
     * Currently, subclasses are required to implement this method, but in the
     * future it may get an empty default implementation to make it optional
     * for subclasses to subscribe to setIndeterminateProgress events.
     */
    protected abstract void _setIndeterminateProgress();

    /**
     * Set visible user message.
     * @param message
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setMessage(String message) {
        _setMessage(message);
        return isCanceled();
    }

    /**
     * Set an image associated with the current progress. A progress listener
     * may choose to optionally display this image wherever is appropriate.
     * @param image an image
     * @return true if the user has requested that this operation be canceled.
     */
    public final boolean setImage(Image image) {
        _setImage(image);
        return isCanceled();
    }

    /**
     *
     * This method is a hook called from {@link #setImage} to allow subclasses a
     * custom reaction to setImage events
     *
     * @param image the image
     */
    protected void _setImage(Image image) {

    }

    /**
     * Adds an action that can choose to provide feedback. For example,
     * an operation may choose to provide a "Skip to next step" button
     * alongside the cancel button. There is no requirement that a
     * ProgressListener actually present this to the user - it may choose
     * to ignore this method, in which case <code> listener </code> will
     * never be fired.
     * @param label a label describing this feedback action. For example, "Skip to next step"
     * @param listener a listener to be notified when the user chooses to invoke
     *                 this action
     */
    public void addFeedbackAction(String label, SimpleListener listener) {

    }

    /**
     * Removes a feedback action previously added using
     * {@link #addFeedbackAction(String, org.virion.jam.util.SimpleListener)}.
     * @param label The  label used as a parameter to {@link #addFeedbackAction(String, org.virion.jam.util.SimpleListener)}
     */
    public void removeFeedbackAction(String label) {

    }



    /**
     * This method is a hook called from {@link #setMessage} to allow subclasses a
     * custom reaction to setMessage events. Currently, subclasses are required to
     * implement this method, but in the future it may get an empty default
     * implementation to make it optional for subclasses to subscribe to setMessage
     * events.
     */
    protected abstract void _setMessage(String message);

    /**
     * This method must be implemented by all subclasses. It is called from
     * {@link #setProgress}, {@link #setIndeterminateProgress} and {@link #setMessage}
     * to determine the return value of these methods.
     *
     * @return true if the user has requested that this operation be canceled.
     */
    public abstract boolean isCanceled();


    /**
     * A ProgressListener that ignores all events and always returns false from
     * {@link #isCanceled}. Useful when you don't care about the progress
     * results or canceling the operation.
     */
    public static final ProgressListener EMPTY = new EmptyProgressListener();

    private static class EmptyProgressListener extends ProgressListener {
        protected void _setProgress(double fractionCompleted) {
        }

        protected void _setMessage(String message) {
        }

        public boolean isCanceled() {
            return false;
        }

        protected void _setIndeterminateProgress() {
        }
    }
}