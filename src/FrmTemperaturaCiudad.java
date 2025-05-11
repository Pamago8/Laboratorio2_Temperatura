import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import datechooser.beans.DateChooserCombo;
import entidades.Temperatura;
import servicios.ServicioTemperaturaCiudad;

public class FrmTemperaturaCiudad extends JFrame {

    private JComboBox cmbCiudad;
    private DateChooserCombo dccDesde, dccHasta;
    private JTabbedPane tpTemperaturaCiudad;
    private JPanel pnlGrafica;
    private JPanel pnlEstadisticas;

    private List<String> ciudades;
    private List<Temperatura> datos;

    public FrmTemperaturaCiudad() {

        setTitle("Temperatura Ciudad");
        setSize(700, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JToolBar tb = new JToolBar();

        JButton btnGraficar = new JButton();
        btnGraficar.setIcon(new ImageIcon(getClass().getResource("/iconos/temperatura.png")));
        btnGraficar.setToolTipText("Grafica Temperaturas vs Fecha");
        btnGraficar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnGraficarClick();
            }
        });
        tb.add(btnGraficar);

        JButton btnCalcularEstadisticas = new JButton();
        btnCalcularEstadisticas.setIcon(new ImageIcon(getClass().getResource("/iconos/temperatura-alta.png")));
        btnCalcularEstadisticas.setToolTipText("Estadísticas de la ciudad seleccionada");
        btnCalcularEstadisticas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnCalcularEstadisticasClick();
            }
        });
        tb.add(btnCalcularEstadisticas);

        // Contenedor con BoxLayout (vertical)
        JPanel pnlTemperaturas = new JPanel();
        pnlTemperaturas.setLayout(new BoxLayout(pnlTemperaturas, BoxLayout.Y_AXIS));

        JPanel pnlDatosProceso = new JPanel();
        pnlDatosProceso.setPreferredSize(new Dimension(pnlDatosProceso.getWidth(), 50)); // Altura fija de 100px
        pnlDatosProceso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlDatosProceso.setLayout(null);

        JLabel lblTemperatura = new JLabel("Ciudad");
        lblTemperatura.setBounds(10, 10, 100, 25);
        pnlDatosProceso.add(lblTemperatura);

        cmbCiudad = new JComboBox();
        cmbCiudad.setBounds(110, 10, 100, 25);
        pnlDatosProceso.add(cmbCiudad);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(220, 10, 100, 25);
        pnlDatosProceso.add(dccDesde);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(330, 10, 100, 25);
        pnlDatosProceso.add(dccHasta);

        pnlGrafica = new JPanel();
        JScrollPane spGrafica = new JScrollPane(pnlGrafica);

        pnlEstadisticas = new JPanel();

        tpTemperaturaCiudad = new JTabbedPane();
        tpTemperaturaCiudad.addTab("Gráfica", spGrafica);
        tpTemperaturaCiudad.addTab("Estadísticas", pnlEstadisticas);

        // Agregar componentes
        pnlTemperaturas.add(pnlDatosProceso);
        pnlTemperaturas.add(tpTemperaturaCiudad);

        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(pnlTemperaturas, BorderLayout.CENTER);

        cargarDatos();
    }

    private void cargarDatos() {
        datos = ServicioTemperaturaCiudad.getDatos(System.getProperty("user.dir") + "/src/datos/Temperaturas.csv");
        ciudades = ServicioTemperaturaCiudad.getCiudades(datos);
        DefaultComboBoxModel dcm = new DefaultComboBoxModel(ciudades.toArray());
        cmbCiudad.setModel(dcm);

    }

    private void btnGraficarClick() {
        if (cmbCiudad.getSelectedIndex() >= 0) {

            String ciudad = (String) cmbCiudad.getSelectedItem();
            LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            var datosFiltrados = ServicioTemperaturaCiudad.filtrar(ciudad, desde, hasta, datos);

            org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            datosFiltrados
                    .forEach(t -> dataset.addValue(t.getTemperatura(), "Temperatura", t.getFecha().format(formato)));

            JFreeChart chart = ChartFactory.createBarChart(
                    "Temperatura de " + ciudad,
                    "Fecha",
                    "°C",
                    dataset);

            // Fijar el rango del eje Y de 0 a 30
            chart.getCategoryPlot().getRangeAxis().setRange(0, 30);

            pnlGrafica.removeAll();
            pnlGrafica.setLayout(new BorderLayout());
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(300, 200)); // Ajusta el tamaño deseado
            pnlGrafica.add(chartPanel, BorderLayout.CENTER);
            pnlGrafica.validate();

            tpTemperaturaCiudad.setSelectedIndex(0);
        }

    }

    private void btnCalcularEstadisticasClick() {
        if (cmbCiudad.getSelectedIndex() >= 0) {

            String ciudad = (String) cmbCiudad.getSelectedItem();
            LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            System.out.println(ciudad + " " + desde + " " + hasta);
            
            tpTemperaturaCiudad.setSelectedIndex(1);

            var estadisticas = ServicioTemperaturaCiudad.getEstadisticas(ciudad, desde, hasta, datos);
            pnlEstadisticas.removeAll();
            pnlEstadisticas.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            int fila = 0;
            for (var estadistica : estadisticas.entrySet()) {
                gbc.gridx = 0;
                gbc.gridy = fila;
                pnlEstadisticas.add(new JLabel(estadistica.getKey()), gbc);
                gbc.gridx = 1;
                pnlEstadisticas.add(new JLabel(String.format("%.2f", estadistica.getValue())), gbc);
                fila++;

            }

           
    }

}
}
