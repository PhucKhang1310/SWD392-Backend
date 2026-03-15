var express = require("express");
var router = express.Router();

router.get("/", function (req, res, next) {
  res.json({
    message: "TechShop BE API is running",
    status: "ok",
  });
});

module.exports = router;
