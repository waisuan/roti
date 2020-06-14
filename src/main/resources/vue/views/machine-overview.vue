<template id="machine-overview">
    <div class="overview-main">
        <div class="container">
            <div class="machine-body" v-for="machine in machines" v-bind:key="machine.serialNumber">
                <form>
                    <fieldset>
                        <div class="row">
                            <div class="column">
                                <label for="serialNumberField">Serial No.</label>
                                <input type="text" id="serialNumberField" v-model="machine.serialNumber" disabled>
                            </div>
                            <div class="column">
                                <label for="customerField">Customer</label>
                                <input type="text" id="customerField" v-model="machine.customer" disabled>
                            </div>
                            <div class="column">
                                <label for="stateField">State</label>
                                <input type="text" id="stateField" v-model="machine.state" disabled>
                            </div>
                        </div>
                        <div class="row">
                            <div class="column">
                                <label for="accTypeField">Acc. Type</label>
                                <input type="text" id="accTypeField" v-model="machine.accountType" disabled>
                            </div>
                            <div class="column">
                                <label for="modelField">Model</label>
                                <input type="text" id="modelField" v-model="machine.model" disabled>
                            </div>
                            <div class="column">
                                <label for="statusField">Status</label>
                                <input type="text" id="statusField" v-model="machine.status" disabled>
                            </div>
                        </div>
                        <div class="row">
                            <div class="column">
                                <label for="brandField">Brand</label>
                                <input type="text" id="brandField" v-model="machine.brand" disabled>
                            </div>
                            <div class="column">
                                <label for="districtField">District</label>
                                <input type="text" id="districtField" v-model="machine.district" disabled>
                            </div>
                            <div class="column">
                                <label for="assigneeField">Assignee</label>
                                <input type="text" id="assigneeField" v-model="machine.personInCharge" disabled>
                            </div>
                        </div>
                        <div class="row">
                            <div class="column">
                                <label for="reporterField">Reporter</label>
                                <input type="text" id="reporterField" v-model="machine.reportedBy" disabled>
                            </div>
                            <div class="column">
                                <label for="createdAtField">Created At</label>
                                <input type="text" id="createdAtField" v-model="machine.createdAt" disabled>
                            </div>
                            <div class="column">
                                <label for="updatedAtField">Updated At</label>
                                <input type="text" id="updatedAtField" v-model="machine.updatedAt" disabled>
                            </div>
                        </div>
<!--                        TODO: additionalNotes, attachment-->
                    </fieldset>
                </form>
            </div>
        </div>
    </div>
</template>
<script>
    Vue.component("machine-overview", {
        template: "#machine-overview",
        data: () => ({
            machines: [],
            pageLimit: 50,
            pageOffset: 0
        }),
        methods: {
            getMachines() {
                axios
                    .get('api/machines', {
                        params: {
                            page_limit: this.pageLimit,
                            page_offset: this.pageOffset
                        }
                    })
                    .then(response => {
                        this.machines = this.machines.concat(response.data)
                        this.pageOffset += this.pageLimit
                        console.log(this.machines)
                    })
            },
            scroll() {
                window.onscroll = () => {
                    let bottomOfWindow = document.documentElement.scrollTop + window.innerHeight === document.documentElement.offsetHeight;

                    if (bottomOfWindow) {
                        this.getMachines()
                    }
                }
            }
        },
        created() {
            this.getMachines()
        },
        mounted() {
            this.scroll()
        }
    });
</script>
<style>
    .overview-main {
        margin: 10px;
    }

    .machine-body {
        border-radius: 10px;
        border-style: solid;
        border-width: thin;
        margin-bottom: 5px;
        padding: 5px;
    }
</style>