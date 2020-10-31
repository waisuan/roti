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
import io.javalin.core.security.SecurityUtil
import models.UserRole

object Routes {
    fun init(app: Javalin) {
        app.routes {
            path("api") {
                // /api/users
                path("users") {
                    get(UserController::getUsers, SecurityUtil.roles(UserRole.ADMIN))
                    put(UserController::updateUsers, SecurityUtil.roles(UserRole.ADMIN))
                    delete(UserController::deleteUsers, SecurityUtil.roles(UserRole.ADMIN))
                    path(":username") {
                        put(UserController::updateUser, SecurityUtil.roles(UserRole.ADMIN))
                        delete(UserController::deleteUser, SecurityUtil.roles(UserRole.ADMIN))
                    }
                    path("register") {
                        post(UserController::createUser, SecurityUtil.roles(UserRole.GUEST))
                    }
                    path("login") {
                        post(UserController::loginUser, SecurityUtil.roles(UserRole.GUEST))
                    }
                    path("logout") {
                        post(
                            UserController::logoutUser,
                            SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                    }
                    path("roles") {
                        get(UserController::getUserRoles, SecurityUtil.roles(UserRole.ADMIN))
                    }
                }

                // /api/machines
                path("machines") {
                    get(
                        MachineController::getAllMachines,
                        SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                    )
                    post(
                        MachineController::createMachine,
                        SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                    )
                    path(":serialNumber") {
                        put(
                            MachineController::updateMachine,
                            SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        delete(
                            MachineController::deleteMachine,
                            SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        path("history") {
                            get(
                                MaintenanceController::getMaintenanceHistory,
                                SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                            post(
                                MaintenanceController::createMaintenanceHistory, SecurityUtil.roles(
                                    UserRole.ADMIN,
                                    UserRole.NON_ADMIN
                                )
                            )
                            path(":workOrderNumber") {
                                put(
                                    MaintenanceController::updateMaintenanceHistory, SecurityUtil.roles(
                                        UserRole.ADMIN,
                                        UserRole.NON_ADMIN
                                    )
                                )
                                delete(
                                    MaintenanceController::deleteMaintenanceHistory, SecurityUtil.roles(
                                        UserRole.ADMIN,
                                        UserRole.NON_ADMIN
                                    )
                                )
                            }
                            path("search") {
                                path(":keyword") {
                                    get(
                                        MaintenanceController::searchMaintenanceHistory, SecurityUtil.roles(
                                            UserRole.ADMIN,
                                            UserRole.NON_ADMIN
                                        )
                                    )
                                }
                            }
                            path("count") {
                                get(
                                    MaintenanceController::getNumberOfRecords,
                                    SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                                )
                            }
                        }
                    }
                    path("search") {
                        path(":keyword") {
                            get(
                                MachineController::searchMachine,
                                SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                        }
                    }
                    path("count") {
                        get(
                            MachineController::getNumberOfMachines,
                            SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                    }
                    path("due") {
                        get(
                            MachineController::getPpmDueMachines,
                            SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        path("count") {
                            get(
                                MachineController::getNumOfPpmDueMachines,
                                SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                        }
                    }
                }

                // /api/files
                path("files") {
                    path(":ownerId") {
                        get(
                            FileController::getFileNames,
                            SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        post(
                            FileController::saveFile,
                            SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                        )
                        path(":fileName") {
                            get(
                                FileController::getFile,
                                SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                            delete(
                                FileController::deleteFile,
                                SecurityUtil.roles(UserRole.ADMIN, UserRole.NON_ADMIN)
                            )
                        }
                    }
                }
            }
        }
    }
}
