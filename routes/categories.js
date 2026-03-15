const express = require("express");
const router = express.Router();
const { getList } = require("../controllers/categoryController");

router.get("/", getList);

module.exports = router;
