package com.roothider.module

/**
 * Central registry of filesystem paths, package names, and other artifacts
 * associated with known root solutions. Add new entries here when a new
 * root method needs coverage — hooks in HookEntry read from these lists
 * instead of hardcoding paths per-solution.
 */
object RootSignatures {

    // Paths that, if found to exist, reveal a root solution to the caller.
    val SUSPICIOUS_PATHS: List<String> = listOf(
        // Magisk
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/adb/magisk",
        "/data/adb/modules",
        "/sbin/.magisk",
        // KernelSU
        "/data/adb/ksu",
        "/data/adb/ksud",
        // KernelSU-Next (fork, distinct data dir in some builds)
        "/data/adb/ksu_next",
        // APatch
        "/data/adb/ap",
        "/data/adb/apd"
    )

    // Package names of manager / companion apps for each root solution.
    val SUSPICIOUS_PACKAGES: List<String> = listOf(
        "com.topjohnwu.magisk",       // Magisk Manager
        "me.weishu.kernelsu",         // KernelSU Manager
        "com.rifsxd.ksunext",         // KernelSU-Next Manager
        "me.bmax.apatch"              // APatch Manager
    )

    // Substrings that should never appear in a command executed via
    // Runtime.exec() / ProcessBuilder when the caller is being shielded.
    val SUSPICIOUS_COMMAND_TOKENS: List<String> = listOf(
        "su", "which su", "ksu", "apd", "magisk"
    )

    // Mount-related keywords worth filtering out of /proc/mounts and
    // /proc/self/mountinfo output — KernelSU/APatch often show up here
    // as overlay entries rather than as a su binary.
    val SUSPICIOUS_MOUNT_KEYWORDS: List<String> = listOf(
        "magisk", "ksu", "apatch", "overlay"
    )
}
