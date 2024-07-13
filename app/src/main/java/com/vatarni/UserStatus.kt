package com.vatarni

data class UserStatus(
    private var name: String?,
    private var profileImage: String?,
    private var lastUpdated: Long?,
    private var statuses: Array<Status>
) {
    fun getName(): String? {
        return name
    }

    fun getProfileImage(): String? {
        return profileImage
    }

    fun getLastUpdated(): Long? {
        return lastUpdated
    }

    fun getStatuses(): Array<Status> {
        return statuses
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun setProfileImage(profileImage: String?) {
        this.profileImage = profileImage
    }

    fun setLastUpdated(lastUpdated: Long?) {
        this.lastUpdated = lastUpdated
    }

    fun setStatuses(statuses: Array<Status>) {
        this.statuses = statuses
    }
}
