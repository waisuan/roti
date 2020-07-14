<template id="machine-overview">
    <div class="overview-main">
        <div class="container">
            <div class="row">
                <div class="column">
                    <input type="text" placeholder="Search for..." v-model.trim="searchFilter" @keyup="search()">
                </div>
            </div>
            <div class="row">
                <div class="column column-20">
                    <select v-model="sortFilter">
                        <option value="id" disabled hidden>Sort by...</option>
                        <option v-for="field in fields" v-bind:value="field.actualField">{{field.prettyField}}</option>
                    </select>
                </div>
                <div class="column column-10">
                    <select v-model="sortOrder">
                        <option value="ASC">ASC</option>
                        <option value="DESC">DESC</option>
                    </select>
                </div>
                <div class="column">
                    <button v-on:click="sort()" :disabled="sortFilter === 'id'"><i class="fa fa-sort"></i> Sort</button>
                </div>
            </div>
            <div class="machine-body" v-for="(machine, index) in machines" v-bind:key="machine.serialNumber">
                <div style="text-align: right">
<!--                    <span><i class="fa fa-plus"></i></span>-->
                    <span><i class="fa fa-minus"></i></span>
                </div>
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
                                <label :for="'tncDateField'+index">TNC Date</label>
                                <input type="text" :id="'tncDateField'+index" v-model="machine.tncDate" disabled>
                            </div>
                            <div class="column">
                                <label :for="'ppmDateField'+index">PPM Date</label>
                                <input type="text" :id="'ppmDateField'+index" v-model="machine.ppmDate" disabled>
                            </div>
                        </div>
                        <div class="row">
                            <div class="column">
                                <div class="float-right">
                                    <small style="color: black">Created at: {{ machine.createdAt }}</small>
                                    |
                                    <small style="color: darkorange">Updated at: {{ machine.updatedAt }}</small>
                                </div>
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
            fields: [],
            pageLimit: 50,
            pageOffset: 0,
            sortFilter: "id",
            sortOrder: "ASC",
            searchFilter: "",
        }),
        methods: {
            search() {
                if (this.searchFilter.length <= 2 && this.searchFilter.length > 0) {
                    return
                }
                this.reset()
                if (this.searchFilter.length === 0) {
                    this.getMachines()
                } else {
                    this.searchMachines()
                }
            },
            sort() {
                this.reset()
                this.getMachines()
            },
            reset() {
                this.machines = []
                this.pageLimit = 50
                this.pageOffset = 0
            },
            searchMachines() {
              axios.get("api/machines/search/" + this.searchFilter).then(response => {
                  this.machines = this.machines.concat(response.data)
              })
            },
            getMachines() {
                axios
                    .get("api/machines", {
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
                                    prettyField: f.replace(/([A-Z])/g, " $1")
                                        .replace(/^./, function (str) {
                                            return str.toUpperCase();
                                        })
                                })
                            })
                        }
                    })
            },
            scroll() {
                window.onscroll = () => {
                    let bottomOfWindow = document.documentElement.scrollTop + window.innerHeight === document.documentElement.offsetHeight;

                    if (bottomOfWindow && this.searchFilter.length === 0) {
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