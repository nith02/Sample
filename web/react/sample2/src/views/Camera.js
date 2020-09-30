import React from 'react';
import {Link} from 'react-router-dom';
import { context } from '../context';
import '../App.css';

class Camera extends React.Component {
    static contextType = context;

    constructor() {
        super();
        this.state = {
            shopName: "",
        }
    }

    render() {
        const { shop_list } = this.context;

        return (
            <div className="App">
                <header className="App-header">
                    <div className="Toolbar">
                        <Link className="HomeButton" to={`/shop/${this.props.match.params.shop}`}>&lt;</Link>
                        {this.props.match.params.camera}
                    </div>
                    <video src="video.mp4" controls autoplay="true"></video>
                </header>
            </div>
        )
    }
}

export default Camera;