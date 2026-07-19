# RootHider

An LSPosed module that hides root / KernelSU / KernelSU-Next / APatch traces
from selected apps.

## How it works

This is **not** a standalone app — it's a plugin loaded by [LSPosed](https://github.com/LSPosed/LSPosed)
into the process of whichever apps you scope it to. LSPosed itself must
already be installed on the device (which in turn requires Magisk or
KernelSU as the root method — that part is outside this module's control).

- `HookEntry.kt` — the `IXposedHookLoadPackage` implementation. This is
  where `File.exists()`, `Runtime.exec()` / `ProcessBuilder`, and
  `PackageManager` queries get intercepted.
- `RootSignatures.kt` — single source of truth for every path, package
  name, and command token associated with a known root solution. Add new
  entries here when a new root method needs coverage instead of touching
  the hook logic.
- `ConfigActivity.kt` — minimal launcher screen; actual enable/scope
  control happens in LSPosed Manager, not in this app's UI.

## Usage (end user)

1. Install LSPosed (requires root already).
2. Install this APK normally.
3. Open **LSPosed Manager → Modules**, enable RootHider, and pick which
   apps it should apply to.
4. Force-stop or reboot the target app so the hook takes effect.

## Building locally

Requires JDK 17 and the Android SDK.

```bash
gradle assembleDebug
```

## Building via GitHub Actions

Every push to `main` (and manual `workflow_dispatch` runs) builds a debug
APK and uploads it as a workflow artifact — see
`.github/workflows/build.yml`. Download the APK from the Actions run's
"Artifacts" section.

## Current coverage

- [x] Basic su binary / Magisk path detection
- [x] Magisk / KernelSU / KernelSU-Next / APatch manager package hiding
- [x] `Runtime.exec` / `ProcessBuilder` command filtering
- [ ] `/proc/mounts` and `/proc/self/mountinfo` overlay filtering (planned)
- [ ] SELinux / `getenforce` spoofing (planned)
- [ ] Play Integrity / SafetyNet response shielding (planned)
