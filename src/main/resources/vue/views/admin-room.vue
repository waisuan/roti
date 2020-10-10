<template id="admin-room">
    <app-frame>
        <div class="container" style="margin-bottom: 80px">
            <table>
                <thead>
                    <tr>
                        <th v-for="header in headers" v-bind:key="header">
                            {{ header | capitalize | clean }}
                        </th>
                        <th>
                            <i class="fa fa-cog"></i>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(user, index) in users" v-bind:key="index">
                        <td>
                            {{ user.username }}
                        </td>
                        <td>
                            <div v-show="isEditing !== user">
                                <span>{{ user.email }}</span>
                            </div>
                            <input type="email"
                                   v-show="isEditing === user"
                                   v-model="user.email">
                        </td>
                        <td>
                            <input type="checkbox" name="userIsApprovedCheckBox"
                                   v-model="user.is_approved"
                                   :disabled="isEditing !== user">
                        </td>
                        <td>
                            <div v-show="isEditing !== user">
                                <span>{{ user.role }}</span>
                            </div>
                            <select v-model="user.role"
                                    v-show="isEditing === user">
                                <option v-for="role in userRoles" v-bind:value="role">
                                    {{ role }}
                                </option>
                            </select>
                        </td>
                        <td>
                            <a href="javascript:void(0)" v-on:click="edit(user)" v-show="isEditing !== user && isDeleting !== user">
                                <i class="fa fa-edit" style="color: green"></i>
                            </a>
                            <a href="javascript:void(0)" v-on:click="remove(user)" v-show="isEditing !== user && isDeleting !== user">
                                <i class="fa fa-trash" style="color: red"></i>
                            </a>

                            <a href="javascript:void(0)" v-on:click="saveInCache(user)" v-show="isEditing === user"><i class="fa fa-save" style="color: green"></i></a>
                            <a href="javascript:void(0)" v-on:click="revertEdit()" v-show="isEditing === user"><i class="fa fa-ban" style="color: red"></i></a>

                            <a href="javascript:void(0)" v-on:click="removeInCache(index)" v-show="isDeleting === user"><i class="fa fa-check" style="color: green"></i></a>
                            <a href="javascript:void(0)" v-on:click="revertRemove()" v-show="isDeleting === user"><i class="fa fa-times" style="color: red"></i></a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div class="footer">
            <div class="container">
                <div class="float-right">
                    <small style="color: red" v-show="hasError">Error! Failed to save changes. Please refresh the page and try again.</small>
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
            isDeleting: null,
            isEditingCache: null,
            cachedChanges: {},
            cachedRemovals: {},
            hasChanges: false,
            isSaving: false,
            hasError: false,
            errorMsg: ""
        }),
        methods: {
            edit(user) {
                if (this.isEditing != null)
                    this.revertEdit()
                this.isEditing = user
                this.isEditingCache = JSON.parse(JSON.stringify(user))
            },
            revertEdit() {
                let index = this.users.indexOf(this.isEditing)
                this.users[index] = this.isEditingCache
                this.isEditing = null
            },
            remove(user) {
                this.isDeleting = user
            },
            revertRemove() {
                this.isDeleting = null
            },
            saveInCache(user) {
                if (JSON.stringify(this.isEditingCache) !== JSON.stringify(user)) {
                    this.cachedChanges[user.username] = user
                    this.hasChanges = true
                }
                this.isEditing = null
            },
            removeInCache(index) {
                let removedUser = this.users[index]
                this.cachedRemovals[removedUser.username] = removedUser
                delete this.cachedChanges[removedUser.username]
                this.users.splice(index, 1)
                this.hasChanges = true
                this.isDeleting = null
            },
            persistChanges() {
              this.isSaving = true

              this.update().then(_ => {
                this.cachedChanges = {}
                this.delete().then(_ => {
                  this.cachedRemovals = {}
                  this.isSaving = false
                  this.hasChanges = false
                }).catch(_ => {
                  this.hasError = true
                  this.isSaving = false
                })
              }).catch(_ => {
                this.hasError = true
                this.isSaving = false
              })
            },
          update() {
            if (Object.keys(this.cachedChanges).length !== 0) {
              return axios.put('api/users', Object.values(this.cachedChanges))
            } else {
              return Promise.resolve()
            }
          },
          delete() {
            if (Object.keys(this.cachedRemovals).length !== 0) {
              const usernames = new URLSearchParams()
              Object.keys(this.cachedRemovals).forEach(username => {
                usernames.append("users", username)
              })
              return axios.delete('api/users', { params: usernames })
            } else {
              return Promise.resolve()
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
            document.title += ' | Admin Panel'
            axios
                .get('api/users')
                .then(response => {
                    this.users = response.data
                    if (this.users.length > 0) {
                        this.headers = Object.keys(this.users[0]).filter(key => key.toLowerCase() !== 'password' && key.toLowerCase() !== 'token')
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