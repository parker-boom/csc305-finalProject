package finalproject;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * ROLE: View.
 * Captures the GitHub folder URL and exposes the analyze trigger for the controller.
 * Controller reads the URL text and listens to the button to start Git fetches.
 *
 * @author Parker Jones
 * @author Ashley Aring
 * @version 3.5
 */
public class SearchBar extends JPanel {

    private final JTextField urlField;
    private final JButton okButton;

    public SearchBar() {
        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(new JLabel("Enter GitHub URL:"), BorderLayout.WEST);

        urlField = new JTextField();
        urlField.setPreferredSize(new Dimension(400, 28));

        okButton = new JButton("Analyze");

        add(urlField, BorderLayout.CENTER);
        add(okButton, BorderLayout.EAST);
    }

    public JButton getOkButton() {
        return okButton;
    }

    public String getUrlText() {
        return urlField.getText().replaceAll("\\s+", "");
    }

    public void setUrlText(String text) {
        urlField.setText(text);
    }
}

