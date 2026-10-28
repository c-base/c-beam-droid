package org.c_base.c_beam.voice;

/**
 * Something the model can ask about the station: Qdrant directly ({@link Knowledge}) or
 * the Sandy Strands assistant ({@link SandyStrands}). Both run off the main thread and
 * report back on it; which one is in use is a setting, see {@link AiServer#knowledge}.
 */
public interface KnowledgeSource {
    interface Callback {
        /** What the model gets to read, or a short note that nothing was found. */
        void onResult(String text);
        void onError(String message);
    }

    void search(String query, Callback callback);

    /** Stops a lookup that is still running; its callback will not fire. */
    void cancel();

    /**
     * Whether the source has read its answer to the user itself (Sandy's voice), so the
     * phone's own voice should not read the model's answer on top of it.
     */
    boolean spokeAloud();

    /** Cuts off any audio the source is still playing. */
    void hush();
}
