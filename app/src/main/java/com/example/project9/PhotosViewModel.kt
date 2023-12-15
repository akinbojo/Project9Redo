package com.example.project9

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class PhotosViewModel : ViewModel(){
    private val TAG = "PhotosViewModel"
    private var auth: FirebaseAuth = Firebase.auth
    private val storage: FirebaseStorage = Firebase.storage
    private val storageRef = storage.reference
    var imagesRef: StorageReference? = storageRef.child("images")
    data class Image(val id: String, val url: Task<Uri>)
    private val _images = MutableLiveData<List<Image>>()
    val images: LiveData<List<Image>>
        get() = _images

    // Get images
    private fun getImages() {
        imagesRef?.listAll()?.addOnSuccessListener { result ->
            val images = result.items.map { fbFile ->
                Image(fbFile.name, fbFile.downloadUrl)
            }
            _images.value = images
        }
    }

    var user: User = User()
    var verifyPassword = ""

    private val _errorHappened = MutableLiveData<String?>()
    val errorHappened: LiveData<String?>
        get() = _errorHappened

    private val _navigateToSignUp = MutableLiveData<Boolean>(false)
    val navigateToSignUp: LiveData<Boolean>
        get() = _navigateToSignUp

    private val _navigateToSignIn = MutableLiveData<Boolean>(false)
    val navigateToSignIn: LiveData<Boolean>
        get() = _navigateToSignIn


    fun isUserLoggedIn(): Boolean{
        return auth.currentUser != null
    }

    init{
        getImages()
    }


    private val _navigateToList = MutableLiveData<Boolean>(false)
    val navigateToList: LiveData<Boolean>
        get() = _navigateToList

    fun onNavigatedToList() {
        _navigateToList.value = false
    }

    fun navigateToSignUp() {
        _navigateToSignUp.value = true
    }

    fun onNavigatedToSignUp() {
        _navigateToSignUp.value = false
    }

    fun navigateToSignIn() {
        _navigateToSignIn.value = true
    }

    fun onNavigatedToSignIn() {
        _navigateToSignIn.value = false
    }

    fun signIn() {
        if (user.email.isEmpty() || user.password.isEmpty()) {
            _errorHappened.value = "Email and password cannot be empty."
            return
        }
        auth.signInWithEmailAndPassword(user.email, user.password).addOnCompleteListener {
            if (it.isSuccessful) {
                _navigateToList.value = true
            } else {
                _errorHappened.value = it.exception?.message
            }
        }
    }

    fun uploadPic(uri: Uri) {
        imagesRef?.putFile(uri)?.addOnFailureListener {
            Log.d(TAG, "Images wasn't uploaded")
        }?.addOnSuccessListener {
            Log.d(TAG, "Image uploaded successfully")
        }
    }

    fun signUp() {
        if (user.email.isEmpty() || user.password.isEmpty()) {
            _errorHappened.value = "Email and password cannot be empty."
            return
        }
        if (user.password != verifyPassword) {
            _errorHappened.value = "Password and verify do not match."
            return
        }
        auth.createUserWithEmailAndPassword(user.email, user.password).addOnCompleteListener {
            if (it.isSuccessful) {
                _navigateToSignIn.value = true
            } else {
                _errorHappened.value = it.exception?.message
            }
        }
    }

    fun signOut() {
        auth.signOut()

        _navigateToSignIn.value = true
    }


    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}