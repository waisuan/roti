<template id="error-page">
    <div class="error-main">
        <div class="error-text">404.</div>
    </div>
</template>
<script>
    Vue.component("error-page", {
        template: "#error-page"
    });
</script>
<style>
    .error-main {
        position:fixed;
        padding:0;
        margin:0;
        top:0;
        left:0;
        width: 100%;
        height: 100%;
        background-color: #95c2de;

        display: grid;
        justify-items: center;
        align-items: center;
    }

    .error-text {
        color: #ffffff;
        font-family: 'Nunito Sans', sans-serif;
        font-size: 11rem;
        position:absolute;
    }
</style>