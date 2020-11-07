package internal

import controllers.FileController
import controllers.MachineController
import controllers.MaintenanceController
import controllers.UserController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.ApiBuilder.put
import io.javalin.core.security.SecurityUtil.roles
import io.javalin.plugin.rendering.vue.VueComponent
import models.UserRole

object Routes {
    fun init(app: Javalin) {
        // Views
        // app.get("/register", VueComponent("<register-user></register-user>"), roles(UserRole.GUEST))
        // app.get("/login", VueComponent("<login-user></login-user>"), roles(UserRole.GUEST))
        app.get("/admin", VueComponent("<admin-room></admin-room>"), roles(UserRole.ADMIN))
        app.get("/machines", VueComponent("<machine-overview></machine-overview>"), roles(UserRole.NON_ADMIN, UserRole.ADMIN))
        app.error(404, "html", VueComponent("<error-page></error-page>"))

        app.routes {
            path("api") {
                // /api/users
                path("users") {
                    get(UserController::getUsers, roles(UserRole.ADMIN))
                    put(UserController::updateUsers, roles(UserRole.ADMIN))
                    delete(UserController::deleteUsers, roles(UserRole.ADMIN))
                    path(":username") {
                        put(UserController::updateUser, roles(UserRole.ADMIN))
                        delete(UserController::deleteUser, roles(UserRole.ADMIN))
                    }
                    path("register") {
                        post(UserController::createUser, roles(UserRole.GUEST))
                    }
                    path("login") {
                        post(UserController::loginUser, roles(UserRole.GUEST))
                    }
                    path("logout") {
                        post(
                            UserController::logoutUser,
                            roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                    }
                    path("roles") {
                        get(UserController::getUserRoles, roles(UserRole.ADMIN))
                    }
                }

                // /api/machines
                path("machines") {
                    get(
                        MachineController::getAllMachines,
                        roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                    )
                    post(
                        MachineController::createMachine,
                        roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                    )
                    path(":serialNumber") {
                        put(
                            MachineController::updateMachine,
                            roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        delete(
                            MachineController::deleteMachine,
                            roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        path("history") {
                            get(
                                MaintenanceController::getMaintenanceHistory,
                                roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                            post(
                                MaintenanceController::createMaintenanceHistory, roles(
                                    UserRole.ADMIN,
                                    UserRole.NON_ADMIN
                                )
                            )
                            path(":workOrderNumber") {
                                put(
                                    MaintenanceController::updateMaintenanceHistory, roles(
                                        UserRole.ADMIN,
                                        UserRole.NON_ADMIN
                                    )
                                )
                                delete(
                                    MaintenanceController::deleteMaintenanceHistory, roles(
                                        UserRole.ADMIN,
                                        UserRole.NON_ADMIN
                                    )
                                )
                            }
                            path("search") {
                                path(":keyword") {
                                    get(
                                        MaintenanceController::searchMaintenanceHistory, roles(
                                            UserRole.ADMIN,
                                            UserRole.NON_ADMIN
                                        )
                                    )
                                }
                            }
                            path("count") {
                                get(
                                    MaintenanceController::getNumberOfRecords,
                                    roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                                )
                            }
                        }
                    }
                    path("search") {
                        path(":keyword") {
                            get(
                                MachineController::searchMachine,
                                roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                        }
                    }
                    path("count") {
                        get(
                            MachineController::getNumberOfMachines,
                            roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                    }
                    path("due") {
                        get(
                            MachineController::getPpmDueMachines,
                            roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        path("count") {
                            get(
                                MachineController::getNumOfPpmDueMachines,
                                roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                        }
                    }
                }

                // /api/files
                path("files") {
                    path(":ownerId") {
                        get(
                            FileController::getFileNames,
                            roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        post(
                            FileController::saveFile,
                            roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        path(":fileName") {
                            get(
                                FileController::getFile,
                                roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                            delete(
                                FileController::deleteFile,
                                roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                        }
                    }
                }
            }
        }
    }
}
