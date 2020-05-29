<template id="register-user">
    <app-frame>
        <div class="container">
            <div class="row">
                <div class="column column-50 column-offset-25">
                    <form @submit.prevent="submit">
                        <fieldset>
                            <label for="usernameField">Username</label>
                            <input type="text" id="usernameField" v-model="userData.username" required>
                            <label for="passwordField">Password</label>
                            <input type="password" id="passwordField" v-model="userData.password" required>
                            <label for="emailField">Email</label>
                            <input type="email" id="emailField" v-model="userData.email" required>
                            <input class="button-primary" type="submit" value="Register">
                            <div class="alert-fail" v-if="submitFailed">
                                <span class="closebtn" onclick="this.parentElement.style.display='none';">&times;</span>
                                Error! {{ errorMsg }}
                            </div>
                            <div class="alert-success" v-if="submitSuccess">
                                <span class="closebtn" onclick="this.parentElement.style.display='none';">&times;</span>
                                Success! You are now registered. We'll email you when your account has been approved.
                                <br/>
                                (<a href="/login" style="color: gold">Go back to login page</a>)
                            </div>
                        </fieldset>
                    </form>
                </div>
            </div>
        </div>
    </app-frame>
</template>
<script>
    Vue.component("register-user", {
        template: "#register-user",
        data: () => ({
            userData: {
                username: null,
                password: null,
                email: null
            },
            submitSuccess: false,
            submitFailed: false,
            errorMsg: ""
        }),
        methods: {
            submit() {
                this.clearSubmitStatus()
                axios
                    .post('api/users/register', this.userData)
                    .then(_ => {
                        this.clearUserData()
                        this.submitSuccess = true
                    })
                    .catch(error => {
                        this.errorMsg = error.response.data
                        this.submitFailed = true
                    });
            },
            clearSubmitStatus() {
                this.submitSuccess = false
                this.submitFailed = false
            },
            clearUserData() {
                this.userData["username"] = null
                this.userData["password"] = null
                this.userData["email"] = null
            }
        }
    });
</script>
<style>
</style>