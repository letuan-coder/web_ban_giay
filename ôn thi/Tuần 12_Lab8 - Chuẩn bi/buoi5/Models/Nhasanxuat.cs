using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Xml.Linq;

namespace buoi5.Models
{
    public partial class Nhasanxuat
    {
        public Nhasanxuat()
        {
            Hanghoa = new HashSet<Hanghoa>();
        }
        [Required(ErrorMessage = "Xin nhập mã nhà sản xuất ")]// rang buoc nhap dl
        [Display(Name = "Mã nhà sản xuất")]
        public string Mansx { get; set; }
        [Required(ErrorMessage = "Xin nhập tên nhà sản xuất ")]// rang buoc nhap dl
        [Display(Name = "Tên nhà sản xuất")]
        public string Tennsx { get; set; }
        [Required(ErrorMessage = "Xin nhập địa chỉ ")]// rang buoc nhap dl
        [Display(Name = "Địa chỉ")]
        public string Diachi { get; set; }

        public ICollection<Hanghoa> Hanghoa { get; set; }
    }
}
