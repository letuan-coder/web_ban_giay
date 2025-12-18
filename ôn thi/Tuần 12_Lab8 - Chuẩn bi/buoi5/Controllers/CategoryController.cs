using Microsoft.AspNetCore.Mvc;
using System.Linq;

namespace buoi5.Controllers
{
    public class CategoryController : Controller
    {
        Models.QLBHContext obj = new Models.QLBHContext();
        public IActionResult Index()
        {
            var lst = obj.Hanghoa.ToList();
            return View(lst);
        }
    }
}
