using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace MyChat
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
            try
            {
                using (var db = new ChatContext())
                {
                    var load = from m in db.Messages select m;
                    dataGrid.ItemsSource = load;
                }
            }
            catch (Exception ex)
            {
                if (ex.InnerException != null) MessageBox.Show(ex.InnerException.ToString());
                throw;
            }
        }
    }
}
