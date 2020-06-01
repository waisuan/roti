<template id="admin-room">
    <app-frame>
        <div class="container">
            <table>
                <thead>
                    <tr>
                        <th v-for="header in headers" v-bind:key="header">
                            {{ header | capitalize | clean }}
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(user, index) in users" v-bind:key="index">
                        <td>
                            {{ user.username }}
                        </td>
                        <td>
                            <div v-show="isEditing !== (index + 'email')">
                                <span v-on:dblclick="edit(index, 'email')">{{ user.email }}</span>
                            </div>
                            <input ref="editEmailCellTag"
                                   type="email"
                                   v-show="isEditing === (index + 'email')"
                                   v-model="user.email"
                                   v-on:blur="revert(index)"
                                   v-on:keyup.enter="saveInCache(user)">
                        </td>
                        <td>
                            <input type="checkbox" name="userIsApprovedCheckBox" v-model="user.is_approved" v-on:click="saveInCache(user)">
                        </td>
                        <td>
                            <div v-show="isEditing !== (index + 'role')">
                                <span v-on:dblclick="edit(index, 'role')">{{ user.role }}</span>
                            </div>
                            <select ref="editRoleCellTag"
                                    v-model="user.role"
                                    v-show="isEditing === (index + 'role')"
                                    v-on:blur="revert(index)"
                                    v-on:keypress.enter="saveInCache(user)">
                                <option v-for="role in userRoles" v-bind:value="role">
                                    {{ role }}
                                </option>
                            </select>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div class="footer">
            <div class="container">
                <div class="float-right">
                    <button class="button"
                            style="margin-top: 7px"
                            :disabled="!hasChanges || isSaving"
                            v-on:click="persistChanges()">
                        <i class="fa fa-refresh fa-spin" v-show="isSaving"></i> Save Changes
                    </button>
                </div>
            </div>
        </div>
    </app-frame>
</template>
<script>
    Vue.component("admin-room", {
        template: "#admin-room",
        data: () => ({
            users: [],
            headers: [],
            userRoles: [],
            isEditing: null,
            isEditingCache: null,
            isSaving: false,
            savedEvent: false,
            hasChanges: false,
            cachedChanges: {}
        }),
        computed: {
        },
        methods: {
            edit(index, value) {
                this.isEditing = index + value
                this.isEditingCache = JSON.parse(JSON.stringify(this.users[index]))
                this.savedEvent = false
                this.$nextTick(_ => {
                    this.$refs.editEmailCellTag[index].focus()
                    this.$refs.editRoleCellTag[index].focus()
                })
            },
            revert(index) {
                if (!this.savedEvent)
                    this.users[index] = this.isEditingCache
            },
            saveInCache(user) {
                this.savedEvent = true
                this.isEditing = null
                this.hasChanges = true
                this.cachedChanges[user.username] = user
            },
            persistChanges() {
                this.isSaving = true

                let count = 0
                let size = Object.keys(this.cachedChanges).length
                for (const [ key, value ] of Object.entries(this.cachedChanges)) {
                    axios.put('api/users/' + key, value).then(_ => {
                        console.log("saved")
                        // count += 1
                        // if (count === size) {
                        //     this.isSaving = false
                        // }
                    }).catch(error => {
                        console.log(error)
                    })
                }
            }
        },
        filters: {
            capitalize(str) {
                return str.charAt(0).toUpperCase() + str.slice(1)
            },
            clean(str) {
                return str.replace("_", " ")
            }
        },
        created() {
            axios
                .get('api/users')
                .then(response => {
                    this.users = response.data
                    if (this.users) {
                        this.headers = Object.keys(this.users[0]).filter(key => key.toLowerCase() !== 'password')
                    }
                })

            axios
                .get('api/users/roles')
                .then(response => {
                    this.userRoles = response.data
                })
        }
    });
</script>
<style>
    .footer {
        background: antiquewhite;
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        height: 50px;
    }
</style>