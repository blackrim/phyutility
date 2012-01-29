package jebl.evolution.align;

import jebl.util.ProgressListener;

/**
 * @author Matt Kearse
 * @version $Id: CompoundAlignmentProgressListener.java 524 2006-11-12 21:15:21Z matt_kearse $
 */
class CompoundAlignmentProgressListener  {
    private boolean cancelled = false;
    private int sectionsCompleted = 0;
    private int totalSections;
    private final ProgressListener progress;
    private int sectionSize= 1;

    public CompoundAlignmentProgressListener(ProgressListener progress, int totalSections) {
        this.totalSections = totalSections;
        this.progress = progress;
    }

    public void setSectionSize(int size) {
        this.sectionSize = size;
    }

    public void incrementSectionsCompleted(int count) {
        sectionsCompleted += count;
    }

    public boolean isCanceled() {
//        return cancelled;
        return progress.isCanceled();
    }

    public ProgressListener getMinorProgress() {
        return minorProgress;
    }

    private ProgressListener minorProgress = new ProgressListener() {
        protected void _setProgress(double fractionCompleted) {
//            System.out.println("progress =" + fractionCompleted+ " sections =" + sectionsCompleted+ "/" + totalSections);
            double totalProgress = (sectionsCompleted + fractionCompleted*sectionSize) / totalSections;
            // if( totalProgress > 1.0 )  System.out.println(totalProgress);
            progress.setProgress(totalProgress);
        }

        protected void _setIndeterminateProgress() {
            progress.setIndeterminateProgress();
        }

        protected void _setMessage(String message) {
            progress.setMessage(message);
        }

        public boolean isCanceled() {
            return progress.isCanceled();
        }
    };
}
