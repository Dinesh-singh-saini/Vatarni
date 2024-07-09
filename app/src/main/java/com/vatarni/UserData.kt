package com.vatarni

class UserData(
    private var name: String? = null,
    private var phoneNumber: String? = null,
    private var email: String? = null,
    private var profilePictureUrl: String? = null,
    private var bio: String? = null
) {

    fun getName(): String? {
        return name
    }

    fun getPhoneNumber(): String? {
        return phoneNumber
    }

    fun getEmail(): String? {
        return email
    }

    fun getProfilePictureUrl(): String? {
        return profilePictureUrl
    }

    fun getBio(): String? {
        return bio
    }

    fun setName(name: String?) {
        this.name = name
    }

    fun setPhoneNumber(phoneNumber: String?) {
        this.phoneNumber = phoneNumber
    }

    fun setEmail(email: String?) {
        this.email = email
    }

    fun setProfilePictureUrl(profilePictureUrl: String?) {
        this.profilePictureUrl = profilePictureUrl
    }

    fun setBio(bio: String?) {
        this.bio = bio
    }
}
