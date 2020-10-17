<template id="machine-overview">
  <div class="overview-main">
    <div class="container">
      <div class="row">
        <div class="column column-60">
          <input type="search" placeholder="Search for..." v-model.trim="searchFilter" v-on:keypress.enter="search()">
        </div>
        <div class="column">
          <button style="margin-bottom: -1px" v-on:click="search()" :disabled="!canSearch()"><i class="fa fa-search"></i> Search</button>
          <div style="text-align: left" v-show="searchFilterIsOn">
            <small><a href="javascript:void(0)" v-on:click="clearSearch()"><i class="fa fa-times-circle"></i> Clear search results</a></small>
          </div>
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
            <button style="margin-bottom: -1px" v-on:click="sort()" :disabled="sortFilter === 'id'"><i class="fa fa-sort"></i> Sort</button>
            <div style="text-align: left" v-show="sortFilterIsOn">
              <small><a href="javascript:void(0)" v-on:click="clearSort()"><i class="fa fa-times-circle"></i> Clear sort filter</a></small>
            </div>
          </div>
        </div>
        <!--TODO Add spinner-->
<!--            <div style="text-align: center; font-size: 100px">-->
<!--                <span><i class="fa fa-spinner fa-pulse"></i></span>-->
<!--            </div>-->
        <div style="text-align: right">
          <small><a href="javascript:void(0)" v-on:click="expandAllForms()">Expand all</a></small>
          |
          <small><a href="javascript:void(0)" v-on:click="collapseAllForms()">Collapse all</a></small>
          |
          <small style="color: black">No. of records: {{machines.length}} / {{totalNoOfMachines}}</small>
        </div>
        <div class="machine-body" v-for="(machine, index) in machines" v-bind:key="machine.serialNumber">
          <div style="text-align: right">
            <a href="javascript:void(0)" v-on:click.prevent="showForm(machine.serialNumber)" v-show="!isFormShown(machine.serialNumber)"><i class="fa fa-plus"></i></a>
            <a href="javascript:void(0)" v-on:click.prevent="hideForm(machine.serialNumber)" v-show="isFormShown(machine.serialNumber)"><i class="fa fa-minus"></i></a>
          </div>
          <div v-show="!isFormShown(machine.serialNumber)">
            <span style="color: dodgerblue">Serial No.: {{machine.serialNumber}}</span>
            |
            <span style="color: black">Created at: {{ machine.createdAt }}</span>
            |
            <span style="color: darkorange">Updated at: {{ machine.updatedAt }}</span>
          </div>
          <form v-show="isFormShown(machine.serialNumber)">
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
<!--                        TODO: Show more... additionalNotes, attachment-->
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
      sortOrder: "DESC",
      searchFilter: "",
      shownForms: {},
      searchFilterIsOn: false,
      totalNoOfMachines: 0,
      sortFilterIsOn: false
    }),
      methods: {
        toggleForm(formId) {
          if (this.isFormShown(formId)) {
            this.hideForm(formId)
          } else {
            this.showForm(formId)
          }
        },
        expandAllForms() {
          this.machines.forEach(machine => {
            this.showForm(machine['serialNumber'])
          })
        },
        collapseAllForms() {
          this.machines.forEach(machine => {
            this.hideForm(machine['serialNumber'])
          })
        },
        showForm(formId) {
          this.$set(this.shownForms, formId, 1)
        },
        hideForm(formId) {
          this.$delete(this.shownForms, formId)
        },
        isFormShown: function(formId)  {
          return formId in this.shownForms
        },
        search() {
          if (!this.canSearch()) {
            return
          }
          this.reset()
          this.searchMachines()
          this.searchFilterIsOn = true
        },
        clearSearch() {
          this.searchFilter = ""
          this.searchFilterIsOn = false
          this.reset()
          this.getMachines()
        },
        canSearch() {
          return this.searchFilter.length > 2
        },
        sort() {
          this.reset()
          if (this.searchFilterIsOn) {
            this.searchMachines()
          } else {
            this.getMachines()
          }
          this.sortFilterIsOn = true
        },
        clearSort() {
          this.sortFilter = "id"
          this.sortOrder = "DESC"
          this.sort()
          this.sortFilterIsOn = false
        },
        reset() {
          this.machines = []
          this.pageLimit = 50
          this.pageOffset = 0
          this.totalNoOfMachines = 0
        },
        searchMachines() {
          axios.get("api/machines/search/" + this.searchFilter, {
            params: {
              page_limit: this.pageLimit,
              page_offset: this.pageOffset,
              sort_filter: this.sortFilter,
              sort_order: this.sortOrder
            }
          }).then(response => {
            this.machines = this.machines.concat(response.data['machines'])
            this.totalNoOfMachines = response.data['count']
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
            }).then(response => {
              this.machines = this.machines.concat(response.data['machines'])
              this.totalNoOfMachines = response.data['count']
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
          // TODO: Use pagination instead
          window.onscroll = () => {
            let bottomOfWindow = document.documentElement.scrollTop + window.innerHeight === document.documentElement.offsetHeight;

            if (bottomOfWindow && this.searchFilter.length === 0) {
              this.getMachines()
            }
          }
        }
      },
      created() {
        document.title += ' | Machines Overview'
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