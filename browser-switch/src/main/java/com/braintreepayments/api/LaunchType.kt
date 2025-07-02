package com.braintreepayments.api

/**
 * Enum representing the type of launch for an activity.
 *
 * - [ACTIVITY_NEW_TASK]: sets the `Intent.FLAG_ACTIVITY_NEW_TASK` flag.
 * - [ACTIVITY_CLEAR_TOP]: sets the `Intent.FLAG_ACTIVITY_CLEAR_TOP` flag.
 */
enum class LaunchType {
    ACTIVITY_NEW_TASK,
    ACTIVITY_CLEAR_TOP
}