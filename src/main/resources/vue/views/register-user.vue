<template id="register-user">
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
                        <small style="color: red" v-if="submitFailed">Error! Something bad happened...</small>
                        <small style="color: green" v-if="submitSuccess">Success! You are now registered.</small>
                    </fieldset>
                </form>
            </div>
        </div>
    </div>
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
            submitFailed: false
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
                    .catch(_ => {
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