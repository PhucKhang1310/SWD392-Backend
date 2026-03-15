const express = require("express");
const router = express.Router();
const {
  list,
  listAll,
  create,
  update,
  remove,
} = require("../controllers/productController");
const { authenticate, authorize } = require("../middleware/auth");

router.get("/admin", authenticate, authorize("admin", "staff"), listAll);
router.get("/", list);
router.post("/", authenticate, authorize("admin", "staff"), create);
router.put("/", authenticate, authorize("admin", "staff"), update);
router.delete("/:id", authenticate, authorize("admin", "staff"), remove);

module.exports = router;
