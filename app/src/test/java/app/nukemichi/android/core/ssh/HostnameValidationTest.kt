package app.nukemichi.android.core.ssh

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [isSafeHostname] is the boundary between a value the app did not author and a script that runs
 * as root on the user's VPS. The candidates it screens come from RealiTLScanner echoing back
 * whatever a neighbour on the VPS's own /24 put in its certificate's CN/SAN — so every rejected
 * case below is an input an attacker can choose, not a hypothetical.
 *
 * Kept exhaustive on purpose: a guard whose failure mode is silent root command execution earns
 * more assertions than its size suggests.
 */
class HostnameValidationTest {

    @Test
    fun `accepts ordinary hostnames`() {
        listOf(
            "example.com",
            "sub.example.com",
            "deep.sub.example.co.uk",
            "xn--80ak6aa92e.com", // punycode — already ASCII by the time it reaches us
            "a.b",
            "host-with-hyphens.example.com",
            "123.example.com",
            "localhost",
        ).forEach { assertTrue("expected accepted: $it", isSafeHostname(it)) }
    }

    @Test
    fun `rejects shell metacharacters`() {
        listOf(
            "example.com;curl evil",
            "example.com|sh",
            "example.com&&whoami",
            "example.com`id`",
            "example.com\$(id)",
            "example.com\${IFS}",
            "example.com>out",
            "example.com<in",
            "example.com'quote",
            "example.com\"quote",
            "example.com\\escape",
            "example.com*glob",
            "example.com?glob",
            "example.com[glob]",
            "example.com(paren)",
            "example.com{brace}",
            "example.com#comment",
            "example.com!bang",
            "example.com~tilde",
            "example.com%percent",
            "example.com^caret",
            "example.com=equals",
            "example.com+plus",
            "example.com,comma",
            "example.com:colon",
            "example.com@at",
            "example.com/slash",
        ).forEach { assertFalse("expected rejected: $it", isSafeHostname(it)) }
    }

    @Test
    fun `rejects whitespace and control characters`() {
        listOf(
            "example .com" to "embedded space",
            " example.com" to "leading space",
            "example.com " to "trailing space",
            "example.com\n" to "trailing newline",
            "\nexample.com" to "leading newline",
            "example.com\r\nHost: evil" to "CRLF injection",
            "example.com\t" to "tab",
            "example.com\u0000" to "NUL byte",
            "example.com\u0000; id" to "NUL-smuggled command",
        ).forEach { (value, description) ->
            assertFalse("expected rejected ($description)", isSafeHostname(value))
        }
    }

    @Test
    fun `rejects malformed label structure`() {
        listOf(
            "",
            " ",
            ".",
            ".example.com",
            "example..com",
            "example.com.", // a valid FQDN, but not a valid literal SNI value
            "-example.com",
            "example-.com",
            "sub.-example.com",
            "sub.example-.com",
        ).forEach { assertFalse("expected rejected: '$it'", isSafeHostname(it)) }
    }

    /** Certificate SANs frequently carry wildcards, and a wildcard is not a usable SNI value. */
    @Test
    fun `rejects wildcards`() {
        listOf("*.example.com", "*", "*.com", "www.*.com")
            .forEach { assertFalse("expected rejected: $it", isSafeHostname(it)) }
    }

    @Test
    fun `enforces the 63-character DNS label limit`() {
        assertTrue(isSafeHostname("${"a".repeat(63)}.com"))
        assertFalse(isSafeHostname("${"a".repeat(64)}.com"))
    }

    @Test
    fun `enforces the 253-character total length limit`() {
        // Four maximum-length labels: 4*63 + 3 dots = 255, two over the limit.
        val tooLong = List(4) { "a".repeat(63) }.joinToString(".")
        assertEquals(255, tooLong.length)
        assertFalse("a 255-character name exceeds the 253 limit", isSafeHostname(tooLong))

        // Trim the first label by two to land exactly on 253.
        val exactlyAtLimit = listOf("a".repeat(61), "a".repeat(63), "a".repeat(63), "a".repeat(63))
            .joinToString(".")
        assertEquals(253, exactlyAtLimit.length)
        assertTrue("253 characters is the limit, not past it", isSafeHostname(exactlyAtLimit))
    }

    @Test
    fun `ShellSafe refuses to wrap an unsafe value`() {
        assertThrows(IllegalArgumentException::class.java) { ShellSafe.of("example.com;rm -rf /") }
        assertThrows(IllegalArgumentException::class.java) { ShellSafe.of("") }
        assertThrows(IllegalArgumentException::class.java) { ShellSafe.of("*.example.com") }
    }

    @Test
    fun `ShellSafe renders an accepted value verbatim`() {
        assertEquals("example.com", ShellSafe.of("example.com").toString())
    }
}
