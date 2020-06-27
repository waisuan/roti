<template id="machine-overview">
    <div class="overview-main">
        <div class="container">
            <div class="row">
                <div class="column column-20">
                    <select v-model="sortFilter">
                        <option value="id" disabled hidden>Sort by...</option>
                        <option v-for="field in fields" v-bind:value="field.actualField">{{field.prettyField}}</option>
                    </select>
                </div>
                <div class="column">
                    <button v-on:click="sort()" :disabled="sortFilter === 'id'"><i class="fa fa-sort"></i> Sort</button>
                </div>
            </div>
            <div class="machine-body" v-for="(machine, index) in machines" v-bind:key="machine.serialNumber">
                <form>
                    <fieldset>
                        <div class="row">
                            <div class="column">
                                <label :for="'serialNumberField'+index">Serial No.</label>
                                <input type="text" :id="'serialNumberField'+index" v-model="machine.serialNumber" disabled>
                            </div>
                            <div class="column">
                                <label :for="'customerField'+index">Customer</label>
                                <input type="text" :id="'customerField'+index" v-model="machine.customer" disabled>
                            </div>
                            <div class="column">
                                <label :for="'stateField'+index">State</label>
                                <input type="text" :id="'stateField'+index" v-model="machine.state" disabled>
                            </div>
                        </div>
                        <div class="row">
                            <div class="column">
                                <label :for="'accTypeField'+index">Acc. Type</label>
                                <input type="text" :id="'accTypeField'+index" v-model="machine.accountType" disabled>
                            </div>
                            <div class="column">
                                <label :for="'modelField'+index">Model</label>
                                <input type="text" :id="'modelField'+index" v-model="machine.model" disabled>
                            </div>
                            <div class="column">
                                <label :for="'statusField'+index">Status</label>
                                <input type="text" :id="'statusField'+index" v-model="machine.status" disabled>
                            </div>
                        </div>
                        <div class="row">
                            <div class="column">
                                <label :for="'brandField'+index">Brand</label>
                                <input type="text" :id="'brandField'+index" v-model="machine.brand" disabled>
                            </div>
                            <div class="column">
                                <label :for="'districtField'+index">District</label>
                                <input type="text" :id="'districtField'+index" v-model="machine.district" disabled>
                            </div>
                            <div class="column">
                                <label :for="'assigneeField'+index">Assignee</label>
                                <input type="text" :id="'assigneeField'+index" v-model="machine.personInCharge" disabled>
                            </div>
                        </div>
                        <div class="row">
                            <div class="column">
                                <label :for="'reporterField'+index">Reporter</label>
                                <input type="text" :id="'reporterField'+index" v-model="machine.reportedBy" disabled>
                            </div>
                            <div class="column">
                                <label :for="'createdAtField'+index">Created At</label>
                                <input type="text" :id="'createdAtField'+index" v-model="machine.createdAt" disabled>
                            </div>
                            <div class="column">
                                <label :for="'updatedAtField'+index">Updated At</label>
                                <input type="text" :id="'updatedAtField'+index" v-model="machine.updatedAt" disabled>
                            </div>
                        </div>
<!--                        TODO: tncDate, ppmDate, additionalNotes, attachment-->
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
            fields: [],
            pageLimit: 50,
            pageOffset: 0,
            sortFilter: "id",
            sortOrder: "ASC"
        }),
        methods: {
            sort() {
                this.reset()
                this.getMachines()
            },
            reset() {
                this.machines = []
                this.fields = []
                this.pageLimit = 50
                this.pageOffset = 0
            },
            getMachines() {
                axios
                    .get('api/machines', {
                        params: {
                            page_limit: this.pageLimit,
                            page_offset: this.pageOffset,
                            sort_filter: this.sortFilter,
                            sort_order: this.sortOrder
                        }
                    })
                    .then(response => {
                        this.machines = this.machines.concat(response.data)
                        this.pageOffset += this.pageLimit
                        if (this.machines.length > 0 && this.fields.length === 0) {
                            Object.keys(this.machines[0]).forEach(f => {
                                this.fields.push({
                                    actualField: f,
                                    prettyField: f.replace(/([A-Z])/g, ' $1')
                                        .replace(/^./, function (str) {
                                            return str.toUpperCase();
                                        })
                                })
                            })
                        }
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