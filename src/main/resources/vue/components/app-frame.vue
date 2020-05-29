<template id="app-frame">
    <div class="app-frame">
        <slot></slot>
    </div>
</template>
<script>
    Vue.component("app-frame", {template: "#app-frame"});
</script>
<style>
    .app-frame {
        margin-top: 10px;
    }

    .alert-fail {
        border-radius: 10px;
        padding: 8px;
        background-color: #f44336; /* Red */
        color: white;
        margin-top: 10px;
    }

    .alert-success {
        border-radius: 10px;
        padding: 8px;
        background-color: lightseagreen; /* Red */
        color: white;
        margin-top: 10px;
    }

    .closebtn {
        margin-left: 10px;
        color: white;
        font-weight: bold;
        float: right;
        font-size: 22px;
        line-height: 20px;
        cursor: pointer;
        transition: 0.3s;
    }

    .closebtn:hover {
        color: black;
    }
</style>