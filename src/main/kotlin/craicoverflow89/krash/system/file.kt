package craicoverflow89.krash.system

class KrashFileSystem {

    companion object {

        fun isAbsolutePath(value: String) = value.replace("\\", "/").let {

            // Nix Path
            if(it.startsWith("/")) return true

            // Windows Path
            if("^[A-Za-z]:[\\/]".toRegex().containsMatchIn(it)) return true

            // Relative Path
            false
        }

    }

}