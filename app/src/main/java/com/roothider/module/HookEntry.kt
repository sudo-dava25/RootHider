package com.roothider.module

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.IOException

class HookEntry : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // Never hook our own module's process, or system_server (out of scope
        // for a Java-level module and risky to touch).
        if (lpparam.packageName == BuildConfigCompat.PACKAGE_NAME) return
        if (lpparam.packageName == "android") return

        // Per-app scoping is normally handled by LSPosed itself (the user
        // picks which apps load this module in LSPosed Manager). These hooks
        // just assume that if we were loaded into this process, this app is
        // in scope and should be shielded.

        hookFileExists(lpparam)
        hookProcessExecution(lpparam)
        hookPackageManager(lpparam)
    }

    /** Hides suspicious paths from java.io.File#exists() and #isDirectory(). */
    private fun hookFileExists(lpparam: LoadPackageParam) {
        val fileClass = File::class.java
        val hook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val file = param.thisObject as File
                val path = file.path
                if (RootSignatures.SUSPICIOUS_PATHS.any { path.startsWith(it) }) {
                    param.result = false
                }
            }
        }
        XposedBridge.hookAllMethods(fileClass, "exists", hook)
        XposedBridge.hookAllMethods(fileClass, "isDirectory", hook)
        XposedBridge.hookAllMethods(fileClass, "canExecute", hook)
    }

    /**
     * Blocks execution attempts that look like a root/su check by making
     * them behave like a "command not found" failure instead of succeeding.
     */
    private fun hookProcessExecution(lpparam: LoadPackageParam) {
        val runtimeClass = Runtime::class.java
        val execHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val cmd = param.args.getOrNull(0) ?: return
                val cmdText = when (cmd) {
                    is Array<*> -> cmd.joinToString(" ") { it.toString() }
                    is String -> cmd
                    else -> return
                }
                if (RootSignatures.SUSPICIOUS_COMMAND_TOKENS.any {
                        cmdText.contains(it, ignoreCase = true)
                    }) {
                    param.throwable = IOException("Cannot run program: not found")
                }
            }
        }
        XposedBridge.hookAllMethods(runtimeClass, "exec", execHook)

        val processBuilderClass = ProcessBuilder::class.java
        XposedBridge.hookAllMethods(processBuilderClass, "start", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val builder = param.thisObject as ProcessBuilder
                val cmdText = builder.command().joinToString(" ")
                if (RootSignatures.SUSPICIOUS_COMMAND_TOKENS.any {
                        cmdText.contains(it, ignoreCase = true)
                    }) {
                    param.throwable = IOException("Cannot run program: not found")
                }
            }
        })
    }

    /** Removes manager packages (Magisk, KernelSU, etc.) from PackageManager queries. */
    private fun hookPackageManager(lpparam: LoadPackageParam) {
        val pmClass = lpparam.classLoader.loadClass("android.app.ApplicationPackageManager")

        XposedBridge.hookAllMethods(pmClass, "getPackageInfo", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val pkgName = param.args.getOrNull(0) as? String ?: return
                if (RootSignatures.SUSPICIOUS_PACKAGES.contains(pkgName)) {
                    param.throwable = android.content.pm.PackageManager.NameNotFoundException()
                }
            }
        })

        XposedBridge.hookAllMethods(pmClass, "getInstalledPackages", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                @Suppress("UNCHECKED_CAST")
                val list = param.result as? MutableList<android.content.pm.PackageInfo> ?: return
                list.removeAll { it.packageName in RootSignatures.SUSPICIOUS_PACKAGES }
            }
        })
    }
}

/** Small stand-in so the module can identify its own package name without a Context. */
object BuildConfigCompat {
    const val PACKAGE_NAME = "com.roothider.module"
}
