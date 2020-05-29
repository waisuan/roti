<template id="login-user">
    <div class="container">
        <div class="row">
            <div class="column column-50 column-offset-25">
                <form v-on:submit.prevent="submit">
                    <fieldset>
                        <label for="usernameField">Username</label>
                        <input type="text" id="usernameField" v-model="userData.username" required>
                        <label for="passwordField">Password</label>
                        <input type="password" id="passwordField" v-model="userData.password" required>
                        <div class="float-right">
                            <input type="checkbox" id="keepSignedInField">
                            <label class="label-inline" for="keepSignedInField">Keep me signed in</label>
                        </div>
                        <input class="button-primary" type="submit" value="Login">
                        <small style="color: red" v-if="submitFailed">Error! Something bad happened...</small>
                        <div>
                            <a href="#">Forgot password?</a>
                        </div>
                    </fieldset>
                </form>
            </div>
        </div>
    </div>
</template>
<script>
    Vue.component("login-user", {
        template: "#login-user",
        data: () => ({
            userData: {
                username: null,
                password: null
            },
            submitFailed: false
        }),
        methods: {
            submit() {
                this.clearSubmitStatus()
                axios
                    .post('api/users/login', this.userData)
                    .then(_ => {
                        this.clearUserData()
                        this.submitSuccess = true
                    })
                    .catch(_ => {
                        this.submitFailed = true
                    });
            },
            clearSubmitStatus() {
                this.submitFailed = false
            },
            clearUserData() {
                this.userData["username"] = null
                this.userData["password"] = null
            }
        }
    });
</script>
<style>
</style>