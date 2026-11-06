package io.mopesbox.Utils;

import net.openhft.affinity.Affinity;

/** Pins the current thread to a CPU core when that core exists; otherwise leaves it unpinned. */
public final class CpuPin {
    private CpuPin() {}

    public static void pin(int cpu) {
        if (cpu >= Runtime.getRuntime().availableProcessors()) return;
        try {
            Affinity.setAffinity(cpu);
        } catch (Throwable t) {
            System.out.println("Could not pin thread to CPU " + cpu + ": " + t.getMessage());
        }
    }
}
